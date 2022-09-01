/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.xml;

import cbit.image.ImageException;
import cbit.image.VCImage;
import cbit.util.xml.VCLogger;
import cbit.util.xml.VCLoggerException;
import cbit.util.xml.XmlUtil;
import cbit.vcell.biomodel.BioModel;
import cbit.vcell.biomodel.ModelUnitConverter;
import cbit.vcell.biomodel.meta.IdentifiableProvider;
import cbit.vcell.biomodel.meta.VCMetaData;
import cbit.vcell.biomodel.meta.xml.XMLMetaDataReader;
import cbit.vcell.biomodel.meta.xml.XMLMetaDataWriter;
import cbit.vcell.field.FieldDataIdentifierSpec;
import cbit.vcell.geometry.Geometry;
import cbit.vcell.geometry.GeometryException;
import cbit.vcell.mapping.MappingException;
import cbit.vcell.mapping.MathMapping;
import cbit.vcell.mapping.MathSymbolMapping;
import cbit.vcell.mapping.SimulationContext;
import cbit.vcell.math.MathDescription;
import cbit.vcell.mathmodel.MathModel;
import cbit.vcell.messaging.server.SimulationTask;
import cbit.vcell.model.Kinetics;
import cbit.vcell.model.ModelUnitSystem;
import cbit.vcell.model.Parameter;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.solver.Simulation;
import cbit.vcell.solver.*;
import cbit.xml.merge.NodeInfo;
import cbit.xml.merge.XmlTreeDiff;
import cbit.xml.merge.XmlTreeDiff.DiffConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.*;
import org.jlibsedml.*;
import org.sbml.jsbml.SBMLException;
import org.vcell.cellml.CellQuanVCTranslator;
import org.vcell.sbml.SbmlException;
import org.vcell.sbml.vcell.MathModel_SBMLExporter;
import org.vcell.sbml.vcell.SBMLAnnotationUtil;
import org.vcell.sbml.vcell.SBMLExporter;
import org.vcell.sbml.vcell.SBMLImportException;
import org.vcell.sbml.vcell.SBMLImporter;
import org.vcell.sedml.SEDMLImporter;
import org.vcell.util.Extent;
import org.vcell.util.Pair;
import org.vcell.util.TokenMangler;
import org.vcell.util.document.VCDocument;
import org.vcell.util.document.VCellSoftwareVersion;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;

/**
 This class represents the 'API' of the XML framework for all VC classes, outside that framework. Most of the methods of
 this class throw an XmlParseException:
 - XMLTo*() and *ToXML() methods: for biomodel, mathmodel, geometry (with the option of including version info)
 for simulation, image.
 - exportXML() methods: exports VCML (as BioModel, MathModel) to either SBML or CellML,
 with the option of specifying an application.
 - importXML() methods: imports XML (as BioModel, MathModel) from either SBML or CellML.
 - compareMerge() methods: compares two VCML documents, with the option of comparing version info.

 * Creation date: (2/26/2004 10:13:28 AM)
 * @author: Rashad Badrawi
 */
public class XmlHelper {

	private final static Logger logger = LogManager.getLogger(XmlHelper.class);

	//represent the containers XML element for the simulation/image data to be imported/exported.
	//For now, same as their VCML counterparts.
	private static final String SIM_CONTAINER = XMLTags.SimulationSpecTag;
	private static final String IMAGE_CONTAINER = XMLTags.GeometryTag;

	//no instances allowed
	private XmlHelper() {}


	public static String bioModelToXML(BioModel bioModel) throws XmlParseException {

		return bioModelToXML(bioModel, true);
	}

	static String getEscapedSoftwareVersion(){
		return TokenMangler.getEscapedString(System.getProperty("vcell.softwareVersion", "unknown"));
	}
	public static String bioModelToXML(BioModel bioModel, boolean printkeys) throws XmlParseException {

		String xmlString = null;

		try {
			Document bioDoc = bioModelToXMLDocument(bioModel, printkeys);
			xmlString = XmlUtil.xmlToString(bioDoc, false);

//			// OLD WAY
//			Element element = xmlProducer.getXML(bioModel);
//			element = XmlUtil.setDefaultNamespace(element, Namespace.getNamespace(XMLTags.VCML_NS));
//			xmlString = XmlUtil.xmlToString(element);
		} catch (Exception e) {
			throw new XmlParseException("Unable to generate Biomodel XML: ", e);
		}
		if (logger.isTraceEnabled()) {
			logger.trace(xmlString);
		}
		return xmlString;
	}


	public static Document bioModelToXMLDocument(BioModel bioModel, boolean printkeys)
			throws XmlParseException, ExpressionException {
		if (bioModel == null){
			throw new IllegalArgumentException("Invalid input for BioModel: " + bioModel);
		}
		// NEW WAY, with XML declaration, vcml element, namespace, version #, etc.
		// create root vcml element
		Element vcmlElement = new Element(XMLTags.VcmlRootNodeTag);
		vcmlElement.setAttribute(XMLTags.VersionTag, getEscapedSoftwareVersion());
		// get biomodel element from xmlProducer and add it to vcml root element
		Xmlproducer xmlProducer = new Xmlproducer(printkeys);
		Element biomodelElement = xmlProducer.getXML(bioModel);
		vcmlElement.addContent(biomodelElement);
		//set namespace for vcmlElement
		vcmlElement = XmlUtil.setDefaultNamespace(vcmlElement, Namespace.getNamespace(XMLTags.VCML_NS));
		// create xml doc with vcml root element and convert to string
		Document bioDoc = new Document();
		Comment docComment = new Comment("This biomodel was generated in VCML Version "+getEscapedSoftwareVersion());
		bioDoc.addContent(docComment);
		bioDoc.setRootElement(vcmlElement);
		return bioDoc;
	}


	//default is to include version information in the comparison.
	public static XmlTreeDiff compareMerge(String xmlBaseString, String xmlModifiedString, DiffConfiguration comparisonSetting) throws XmlParseException {

		return compareMerge(xmlBaseString, xmlModifiedString, comparisonSetting, false);
	}


	/**
	 * compareMerge method comment.
	 */
	public static cbit.xml.merge.XmlTreeDiff compareMerge(String xmlBaseString, String xmlModifiedString,
														  DiffConfiguration comparisonSetting, boolean ignoreVersionInfo) throws XmlParseException {
		try {
			if (xmlBaseString == null || xmlModifiedString == null || xmlBaseString.length() == 0 || xmlModifiedString.length() == 0 ||
					(!DiffConfiguration.COMPARE_DOCS_SAVED.equals(comparisonSetting) && !DiffConfiguration.COMPARE_DOCS_OTHER.equals(comparisonSetting)) ) {
				throw new XmlParseException("Invalid XML comparison params.");
			}
			XMLSource xmlBaseSource = new XMLSource(xmlBaseString);
			XMLSource xmlModifiedSource = new XMLSource(xmlModifiedString);

			Element baselineRoot = xmlBaseSource.getXmlDoc().getRootElement();            //default setting, no validation
			Element modifiedRoot = xmlModifiedSource.getXmlDoc().getRootElement();
			//Merge the Documents
			XmlTreeDiff merger = new XmlTreeDiff(ignoreVersionInfo);
			@SuppressWarnings("unused")
			NodeInfo top = merger.merge(baselineRoot.getDocument(), modifiedRoot.getDocument(), comparisonSetting);

			// Return the result
			return merger;                                               //return the tree-diff instead of the root node.
		} catch (Exception e) {
			throw new XmlParseException(e);
		}

	}

	public static Pair <String, Map<Pair <String, String>, String>> exportSBMLwithMap(
			VCDocument vcDoc, int level, int version, int pkgVersion, boolean isSpatial,
			SimulationContext simContext, SimulationJob simJob, boolean bRoundTripValidation) throws XmlParseException {
		return translateSBML(vcDoc, level, version, simContext, simJob, bRoundTripValidation);
	}


	/**
	 * Exports VCML format to another supported format (currently: SBML or CellML). It allows
	 choosing a specific Simulation Spec to export.
	 * Creation date: (4/8/2003 12:30:27 PM)
	 * @return java.lang.String
	 */
	public static String exportSBML(VCDocument vcDoc, int level, int version, int pkgVersion, boolean isSpatial, SimulationContext simContext, SimulationJob simJob, boolean bRoundTripValidation) throws XmlParseException {
		return translateSBML(vcDoc, level, version, simContext, simJob, bRoundTripValidation).one;
	}


	private static Pair <String, Map<Pair <String, String>, String>> translateSBML(VCDocument vcDoc, int level, int version, SimulationContext simContext,
			SimulationJob simJob, boolean bRoundTripValidation) throws XmlParseException {
		if (vcDoc == null) {
			throw new XmlParseException("Invalid arguments for exporting SBML.");
		}
		if (vcDoc instanceof BioModel) {
			try {
				// we don't use the spatial flag anymore
				// TODO: make better use of the spatial flag in the future

				// for now we have only one single combination of version and unit system that works well
				// TODO: check whether we already have these units
				// TODO: make unit change optional
				// TODO: (maybe) support more than one version?
				BioModel bm = simContext.getBioModel();
				BioModel sbmlPreferredUnitsBM = ModelUnitConverter.createBioModelWithSBMLUnitSystem(bm);
				if(sbmlPreferredUnitsBM == null) {
					throw new RuntimeException("Unable to clone BioModel: " + bm.getName());
				}

				// clone BioModel
				BioModel clonedBioModel = cloneBioModel(sbmlPreferredUnitsBM);
				// extract the simContext from new Biomodel. Apply overrides to *this* modified simContext
				SimulationContext simContextFromClonedBioModel = clonedBioModel.getSimulationContext(simContext.getName());
				SimulationContext clonedSimContext = applyOverridesForSBML(clonedBioModel, simContextFromClonedBioModel, simJob);
				// extract sim (in simJob) from modified Biomodel, if not null
				SimulationJob modifiedSimJob = null;
				if (simJob != null) {
					Simulation simFromClonedBiomodel = clonedSimContext.getSimulation(simJob.getSimulation().getName());
					modifiedSimJob = new SimulationJob(simFromClonedBiomodel, simJob.getJobIndex(), null);
				}
				
				SBMLExporter sbmlExporter = new SBMLExporter(clonedSimContext, level, version, bRoundTripValidation);
				sbmlExporter.setSelectedSimulationJob(modifiedSimJob);
				String sbmlString  = sbmlExporter.getSBMLString();

				// cleanup the string of all the "sameAs" statements
				sbmlString = SBMLAnnotationUtil.postProcessCleanup(sbmlString);
				return new Pair(sbmlString, sbmlExporter.getLocalToGlobalTranslationMap());
			} catch (SbmlException | SBMLException | XMLStreamException | ExpressionException e) {
				throw new XmlParseException(e);
			}
		} else if (vcDoc instanceof MathModel) {
			try {
				return new Pair(MathModel_SBMLExporter.getSBMLString((MathModel)vcDoc, level, version), null);
			} catch (ExpressionException | IOException | SBMLException | XMLStreamException e) {
				throw new XmlParseException(e);
			}
		} else{
			throw new RuntimeException("unsupported Document Type "+vcDoc.getClass().getName()+" for SBML export");
		}
	}

	/**
	 * applyOverrides: private method to apply overrides from the simulation in 'simJob' to simContext, if any.
	 * 				Start off by cloning biomodel, since all the references are required in cloned simContext and is
	 * 				best retained by cloning biomodel.
	 * @param bm - biomodel to be cloned
	 * @param sc - simulationContext to be cloned and overridden using math overrides in simulation
	 * @param simJob - simulationJob from where simulation with overrides is obtained.
	 * @return
	 */
	public static SimulationContext applyOverridesForSBML(BioModel bm, SimulationContext sc, SimulationJob simJob) {
		SimulationContext overriddenSimContext = sc;
		if (simJob != null ) {
			Simulation sim = simJob.getSimulation();
			// need to clone Biomodel, simContext, etc. only if simulation has override(s)
			try {
				if (sim != null && sim.getMathOverrides().hasOverrides()) {
//				BioModel clonedBM = (BioModel)BeanUtils.cloneSerializable(bm);
					BioModel clonedBM = XMLToBioModel(new XMLSource(bioModelToXML(bm)));
					clonedBM.refreshDependencies();
					// get the simContext in cloned Biomodel that corresponds to 'sc'
					SimulationContext[] simContexts = clonedBM.getSimulationContexts();
					for (int i = 0; i < simContexts.length; i++) {
						if (simContexts[i].getName().equals(sc.getName())) {
							overriddenSimContext = simContexts[i];
							break;
						}
					}
					//
					overriddenSimContext.getModel().refreshDependencies();
					overriddenSimContext.refreshDependencies();
					MathMapping mathMapping = overriddenSimContext.createNewMathMapping();
					MathSymbolMapping msm = mathMapping.getMathSymbolMapping();

					MathOverrides mathOverrides = sim.getMathOverrides();
					String[] moConstNames = mathOverrides.getOverridenConstantNames();
					for (int i = 0; i < moConstNames.length; i++){
						cbit.vcell.math.Constant overriddenConstant = mathOverrides.getConstant(moConstNames[i]);
						// Expression overriddenExpr = mathOverrides.getActualExpression(moConstNames[i], 0);
						Expression overriddenExpr = mathOverrides.getActualExpression(moConstNames[i], simJob.getJobIndex());
						// The above constant (from mathoverride) is not the same instance as the one in the MathSymbolMapping hash.
						// Hence retreive the correct instance from mathSymbolMapping (mathMapping -> mathDescription) and use it to
						// retrieve its value (symbolTableEntry) from hash.
						cbit.vcell.math.Variable overriddenVar = msm.findVariableByName(overriddenConstant.getName());
						cbit.vcell.parser.SymbolTableEntry[] stes = msm.getBiologicalSymbol(overriddenVar);
						if (stes == null) {
							throw new NullPointerException("No matching biological symbol for : " + overriddenConstant.getName());
						}
						if (stes.length > 1) {
							throw new RuntimeException("Cannot have more than one mapping entry for constant : " + overriddenConstant.getName());
						}
						if (stes[0] instanceof Parameter) {
							Parameter param = (Parameter)stes[0];
							if (param.isExpressionEditable()) {
								if (param instanceof Kinetics.KineticsParameter) {
									// Kinetics param has to be set separately for the integrity of the kinetics object
									Kinetics.KineticsParameter kinParam = (Kinetics.KineticsParameter)param;
									ReactionStep[] rs = overriddenSimContext.getModel().getReactionSteps();
									for (int j = 0; j < rs.length; j++){
										if (rs[j].getNameScope().getName().equals(kinParam.getNameScope().getName())) {
											rs[j].getKinetics().setParameterValue(kinParam, overriddenExpr);
										}
									}
								} else if (param instanceof cbit.vcell.model.ExpressionContainer) {
									// If it is any other editable param, set its expression with the
									((cbit.vcell.model.ExpressionContainer)param).setExpression(overriddenExpr);
								}
							}
						}	// end - if (stes[0] is Parameter)
					}	// end  - for moConstNames
				} 	// end if (sim has MathOverrides)
			} catch (Exception e) {
				throw new RuntimeException("Could not apply overrides from simulation to application parameters : " + e.getMessage(), e);
			}
		}	// end if (simJob != null)
		return overriddenSimContext;
	}

	/**
	 * Exports VCML format to another supported format (currently: SBML or CellML). It allows
	 choosing a specific Simulation Spec to export.
	 * Creation date: (4/8/2003 12:30:27 PM)
	 * @return java.lang.String
	 */
	public static String exportCellML(VCDocument vcDoc, String appName) throws XmlParseException {
		throw new RuntimeException("CellML support has been disabled");
	}


	public static String geometryToXML(Geometry geometry) throws XmlParseException {

		return geometryToXML(geometry, true);
	}


	static String geometryToXML(Geometry geometry, boolean printkeys) throws XmlParseException {

		String geometryString = null;

		if (geometry == null){
			throw new XmlParseException("Invalid input for Geometry: " + geometry);
		}

		// NEW WAY, with XML declaration, vcml element, namespace, version #, etc.
		// create the root vcml element
		Element vcmlElement = new Element(XMLTags.VcmlRootNodeTag);
		vcmlElement.setAttribute(XMLTags.VersionTag, getEscapedSoftwareVersion());
		// get the geometry element from xmlProducer
		Xmlproducer xmlProducer = new Xmlproducer(printkeys);
		Element geometryElement = xmlProducer.getXML(geometry);
		// add it to root vcml element
		vcmlElement.addContent(geometryElement);
		//set default namespace for vcmlElemebt
		vcmlElement = XmlUtil.setDefaultNamespace(vcmlElement, Namespace.getNamespace(XMLTags.VCML_NS));
		// create xml doc using vcml root element and convert to string
		Document geoDoc = new Document();
		Comment docComment = new Comment("This geometry was generated in VCML Version "+getEscapedSoftwareVersion());
		geoDoc.addContent(docComment);
		geoDoc.setRootElement(vcmlElement);
		geometryString = XmlUtil.xmlToString(geoDoc, false);

//		// OLD WAY
//		Element element = xmlProducer.getXML(geometry);
//		element = XmlUtil.setDefaultNamespace(element, Namespace.getNamespace(XMLTags.VCML_NS));
//		geometryString = XmlUtil.xmlToString(element);

		return geometryString;
	}

	public static String imageToXML(VCImage vcImage) throws XmlParseException {

		return imageToXML(vcImage, true);
	}


	static String imageToXML(VCImage vcImage, boolean printKeys) throws XmlParseException {

		String xmlString = null;

		if (vcImage == null){
			throw new XmlParseException("Invalid input for VCImage: " + vcImage);
		}
		Xmlproducer xmlProducer = new Xmlproducer(printKeys);
		Extent extent = vcImage.getExtent();
		Element container = new Element(IMAGE_CONTAINER);
		Element imageElement = xmlProducer.getXML(vcImage);
		Element extentElement = xmlProducer.getXML(extent);
		container.addContent(imageElement);
		container.addContent(extentElement);
		container = XmlUtil.setDefaultNamespace(container, Namespace.getNamespace(XMLTags.VCML_NS));
		xmlString = XmlUtil.xmlToString(container);

		return xmlString;
	}

	public static String vcMetaDataToXML(VCMetaData vcMetaData, IdentifiableProvider identifiableProvider) throws XmlParseException {

		String xmlString = null;
		Element vcMetaDataElement = XMLMetaDataWriter.getElement(vcMetaData, identifiableProvider);
		vcMetaDataElement = XmlUtil.setDefaultNamespace(vcMetaDataElement, Namespace.getNamespace(XMLTags.VCML_NS));
		xmlString = XmlUtil.xmlToString(vcMetaDataElement);

		return xmlString;
	}

	public static VCMetaData xmlToVCMetaData(VCMetaData populateThisVCMetaData,BioModel bioModel,String vcMetaDataXML) throws XmlParseException{
		Document vcMetaDataDoc = XmlUtil.stringToXML(vcMetaDataXML,null);
		XMLMetaDataReader.readFromElement(populateThisVCMetaData, bioModel, vcMetaDataDoc.getRootElement());
		return populateThisVCMetaData;
	}
	/**
	 Allows the translation process to interact with the user via TranslationMessager
	 */
	public static VCDocument importSBML(VCLogger vcLogger, XMLSource xmlSource, boolean bSpatial) throws XmlParseException, VCLoggerException, IOException {

		//checks that the source is not empty
		if (xmlSource == null){
			throw new XmlParseException("Invalid params for importing sbml model.");
		}

		// First try getting xmlfile from xmlSource. If not file, get xmlStr and save it in file
		// (since we send only file name to SBMLImporter). If xmlString is also not present in xmlSource, throw exception.
		File sbmlFile = xmlSource.getXmlFile();
		if (sbmlFile == null) {
			String sbmlStr = xmlSource.getXmlString();
			if (sbmlStr != null) {
				sbmlFile = File.createTempFile("temp", ".xml");
				sbmlFile.deleteOnExit();
				XmlUtil.writeXMLStringToFile(sbmlStr, sbmlFile.getAbsolutePath(), true);
			} else {
				throw new RuntimeException("Error importing from SBML : no SBML source.");
			}
		}
		VCDocument vcDoc = null;
		boolean bValidateSBML = false;
		SBMLImporter sbmlImporter = new SBMLImporter(sbmlFile.getAbsolutePath(), vcLogger, bValidateSBML);
		vcDoc = sbmlImporter.getBioModel();

		vcDoc.refreshDependencies();
		logger.info("Succesful model import: SBML file "+sbmlFile);
		return vcDoc;
	}

	public static VCDocument importBioCellML(VCLogger vcLogger, XMLSource xmlSource) throws Exception {
		throw new Exception("CellML import to a Bio-Model has been disabled.");
	}

	public static VCDocument importMathCellML(VCLogger vcLogger, XMLSource xmlSource) throws Exception {
		// throw new Exception("CellML support has been disabled.");

		//checks that the string is not empty
		if (xmlSource == null){
			throw new XmlParseException("Invalid params for importing cellml model to Mathmodel.");
		}
		Document xmlDoc = xmlSource.getXmlDoc();
		String xmlString = XmlUtil.xmlToString(xmlDoc, false);
		CellQuanVCTranslator cellmlTranslator = new CellQuanVCTranslator();
		VCDocument vcDoc = cellmlTranslator.translate(new StringReader(xmlString), false);
		vcDoc.refreshDependencies();
		return vcDoc;
	}

	public static String mathModelToXML(MathModel mathModel) throws XmlParseException {
		return mathModelToXML(mathModel, true);
	}

	static String mathModelToXML(MathModel mathModel, boolean printkeys) throws XmlParseException {

		String xmlString = null;

		if (mathModel == null){
			throw new XmlParseException("Invalid input for BioModel: " + mathModel);
		}
		// NEW WAY, with XML declaration, vcml element, namespace, version #, etc.
		// create root vcml element
		Element vcmlElement = new Element(XMLTags.VcmlRootNodeTag);
		vcmlElement.setAttribute(XMLTags.VersionTag, getEscapedSoftwareVersion());
		// get mathmodel element from xmlProducer and add it to vcml root element
		Xmlproducer xmlProducer = new Xmlproducer(printkeys);
		Element mathElement = xmlProducer.getXML(mathModel);
		vcmlElement.addContent(mathElement);
		//set namespace for vcmlElement
		vcmlElement = XmlUtil.setDefaultNamespace(vcmlElement, Namespace.getNamespace(XMLTags.VCML_NS));
		// create xml doc with vcml root element and convert to string
		Document mathDoc = new Document();
		Comment docComment = new Comment("This mathmodel was generated in VCML Version "+getEscapedSoftwareVersion());
		mathDoc.addContent(docComment);
		mathDoc.setRootElement(vcmlElement);
		xmlString = XmlUtil.xmlToString(mathDoc, false);

//		// OLD WAY
//		Element element = xmlProducer.getXML(mathModel);
//		element = XmlUtil.setDefaultNamespace(element, Namespace.getNamespace(XMLTags.VCML_NS_ALT));
//		xmlString = XmlUtil.xmlToString(element);
		if (logger.isTraceEnabled()) logger.trace(xmlString);
		return xmlString;
	}


	public static String simToXML(Simulation sim) throws XmlParseException {

		String simString = null;

		if (sim == null) {
			throw new XmlParseException("Invalid input for Simulation: " + sim);
		}
		Xmlproducer xmlProducer = new Xmlproducer(true);
		MathDescription md = sim.getMathDescription();           //cannot be null
		Geometry geom = md.getGeometry();
		Element container = new Element(SIM_CONTAINER);
		Element mathElement = xmlProducer.getXML(md);
		Element simElement = xmlProducer.getXML(sim);
		if (geom != null) {
			Element geomElement = xmlProducer.getXML(geom);
			container.addContent(geomElement);
		} else {
			logger.error("No corresponding geometry for the simulation: " + sim.getName());
		}
		container.addContent(mathElement);
		container.addContent(simElement);
		container = XmlUtil.setDefaultNamespace(container, Namespace.getNamespace(XMLTags.VCML_NS));
		simString = XmlUtil.xmlToString(container);

		return simString;
	}

	public static List<BioModel> readOmex(File omexFile, VCLogger vcLogger) throws Exception {
		List<BioModel> docs = new ArrayList<>();
		// iterate through one or more SEDML objects
		ArchiveComponents ac = null;
		ac = Libsedml.readSEDMLArchive(new FileInputStream(omexFile));
		List<SEDMLDocument> sedmlDocs = ac.getSedmlDocuments();


		List<SedML> sedmls = new ArrayList<>();
		for (SEDMLDocument sedmlDoc : sedmlDocs) {
			SedML sedml = sedmlDoc.getSedMLModel();
			if (sedml == null) {
				throw new RuntimeException("Failed importing " + omexFile.getName());
			}
			if (sedml.getModels().isEmpty()) {
				throw new RuntimeException("Unable to find any model in " + omexFile.getName());
			}
			sedmls.add(sedml);
		}
		for (SedML sedml : sedmls) {
			// default to import all tasks
			ExternalDocInfo externalDocInfo = new ExternalDocInfo(omexFile,true);
			List<BioModel> biomodels = importSEDML(vcLogger, externalDocInfo, sedml, false);
			for (BioModel biomodel : biomodels) {
				docs.add(biomodel);
			}
		}
		return docs;
	}


	public static List<BioModel> importSEDML(VCLogger transLogger, ExternalDocInfo externalDocInfo,
	   SedML sedml, boolean exactMatchOnly) throws Exception {
		SEDMLImporter sedmlImporter = new SEDMLImporter(transLogger, externalDocInfo,
				sedml, exactMatchOnly);
		return sedmlImporter.getBioModels();
	}

	public static BioModel XMLToBioModel(XMLSource xmlSource) throws XmlParseException {
		return XMLToBioModel(xmlSource, true, null);
	}

	public static BioModel cloneBioModel(BioModel origBiomodel) throws XmlParseException {
		String biomodelXMLString = bioModelToXML(origBiomodel);
		XMLSource newXMLSource = new XMLSource(biomodelXMLString);
		return XMLToBioModel(newXMLSource);
	}


	public static BioModel XMLToBioModel(XMLSource xmlSource, boolean printkeys, ModelUnitSystem forcedModelUnitSystem) throws XmlParseException {

		//long l0 = System.currentTimeMillis();
		BioModel bioModel = null;

		if (xmlSource == null){
			throw new XmlParseException("Invalid xml for Biomodel.");
		}

		Document xmlDoc = xmlSource.getXmlDoc();

		// NOTES:
		//	* The root element can be <Biomodel> (old-style vcml) OR <vcml> (new-style vcml)
		//	* With the old-style vcml, the namespace was " "
		// 	* With the new-style vcml, there is an intermediate stage where the namespace for <vcml> root
		//		was set to "http://sourceforge.net/projects/VCell/version0.4" for some models and
		//		"http://sourceforge.net/projects/vcell/vcml" for some models; and the namespace for child element
		//	 	<biomdel>, etc. was " "
		// 	* The final new-style vcml has (should have) the namespace "http://sourceforge.net/projects/vcell/vcml"
		//		for <vcml> and all children elements.
		// The code below attempts to take care of this situation.
		Element root = xmlDoc.getRootElement();
		Namespace ns = null;
		if (root.getName().equals(XMLTags.VcmlRootNodeTag)) {
			// NEW WAY - with xml string containing xml declaration, vcml element, namespace, etc ...
			ns = root.getNamespace();
			Element bioRoot = root.getChild(XMLTags.BioModelTag, ns);
			if (bioRoot == null) {
				bioRoot = root.getChild(XMLTags.BioModelTag);
				//	bioRoot was null, so obtained the <Biomodel> element with namespace " ";
				//	Re-set the namespace so that the correct XMLReader constructor is invoked.
				ns = null;
			}
			root = bioRoot;
		} 	// else - root is assumed to be old-style vcml with <Biomodel> as root.

		// common for both new way (with xml declaration, vcml element, etc) and existing way (biomodel is root)
		// If namespace is null, xml is the old-style xml with biomodel as root, so invoke XMLReader without namespace argument.
		XmlReader reader = null;
		VCellSoftwareVersion vCellSoftwareVersion = null;
		if (ns == null) {
			reader = new XmlReader(printkeys);
		} else {
			reader = new XmlReader(printkeys, ns);
			vCellSoftwareVersion = VCellSoftwareVersion.fromString(root.getAttributeValue(XMLTags.SoftwareVersionAttrTag,ns));
		}
		if (forcedModelUnitSystem != null) {
			reader.setForcedModelUnitSystem(forcedModelUnitSystem);
		}
		bioModel = reader.getBioModel(root,vCellSoftwareVersion);

		//long l1 = System.currentTimeMillis();
		bioModel.refreshDependencies();
		//long l2 = System.currentTimeMillis();
		//logger.trace("refresh-------- "+((double)(l2-l1))/1000);
		//logger.trace("total-------- "+((double)(l2-l0))/1000);

		return bioModel;
	}


	/**
	 * Insert the method's description here.
	 * Creation date: (2/7/2006 4:45:26 PM)
	 * @return cbit.vcell.document.VCDocument
	 * @param xmlString java.lang.String
	 */
	public static VCDocument XMLToDocument(VCLogger vcLogger, String xmlString) throws Exception {
		VCDocument doc = null;
		XMLSource xmlSource = new XMLSource(xmlString);
		org.jdom.Element rootElement = xmlSource.getXmlDoc().getRootElement();         //some overhead.
		String xmlType = rootElement.getName();
		if (xmlType.equals(XMLTags.VcmlRootNodeTag)) {
			// For now, assuming that <vcml> element has only one child (biomodel, mathmodel or geometry).
			// Will deal with multiple children of <vcml> Element when we get to model composition.
			java.util.List<?> childElementList = rootElement.getChildren();
			Element modelElement = (Element)childElementList.get(0);	// assuming first child is the biomodel, mathmodel or geometry.
			xmlType = modelElement.getName();
		}
		if (xmlType.equals(XMLTags.BioModelTag)) {
			doc = XmlHelper.XMLToBioModel(xmlSource);
		} else if (xmlType.equals(XMLTags.MathModelTag)) {
			doc = XmlHelper.XMLToMathModel(xmlSource);
		} else if (xmlType.equals(XMLTags.GeometryTag)) {
			doc = XmlHelper.XMLToGeometry(xmlSource);
		} else if (xmlType.equals(XMLTags.SbmlRootNodeTag)) {
			Namespace namespace = rootElement.getNamespace(XMLTags.SBML_SPATIAL_NS_PREFIX);
			boolean bIsSpatial = (namespace==null) ? false : true;
			doc = XmlHelper.importSBML(vcLogger, xmlSource, bIsSpatial);
		} else if (xmlType.equals(XMLTags.CellmlRootNodeTag)) {
			doc = XmlHelper.importMathCellML(vcLogger, xmlSource);
		} else { // unknown XML format
			throw new RuntimeException("unsupported XML format, first element tag is <"+rootElement.getName()+">");
		}
		return doc;
	}



	public static Geometry XMLToGeometry(XMLSource xmlSource) throws XmlParseException {

		return XMLToGeometry(xmlSource, true);
	}


	static Geometry XMLToGeometry(XMLSource xmlSource, boolean printkeys) throws XmlParseException {

		Geometry geometry = null;

		if (xmlSource == null){
			throw new XmlParseException("Invalid xml for Geometry.");
		}

		Document xmlDoc = xmlSource.getXmlDoc();

		// NOTES:
		//	* The root element can be <Biomodel> (old-style vcml) OR <vcml> (new-style vcml)
		//	* With the old-style vcml, the namespace was " "
		// 	* With the new-style vcml, there is an intermediate stage where the namespace for <vcml> root
		//		was set to "http://sourceforge.net/projects/VCell/version0.4" for some models and
		//		"http://sourceforge.net/projects/vcell/vcml" for some models; and the namespace for child element
		//	 	<biomdel>, etc. was " "
		// 	* The final new-style vcml has (should have) the namespace "http://sourceforge.net/projects/vcell/vcml"
		//		for <vcml> and all children elements.
		// The code below attempts to take care of this situation.
		Element root = xmlDoc.getRootElement();
		Namespace ns = null;
		if (root.getName().equals(XMLTags.VcmlRootNodeTag)) {
			// NEW WAY - with xml string containing xml declaration, vcml element, namespace, etc ...
			ns = root.getNamespace();
			Element geoRoot = root.getChild(XMLTags.GeometryTag, ns);
			if (geoRoot == null) {
				geoRoot = root.getChild(XMLTags.GeometryTag);
				//	geoRoot was null, so obtained the <Geometry> element with namespace " ";
				//	Re-set the namespace so that the correct XMLReader constructor is invoked.
				ns = null;
			}
			root = geoRoot;
		} 	// else - root is assumed to be old-style vcml with <Geometry> as root.

		// common for both new-style (with xml declaration, vcml element, etc) and old-style (geometry is root)
		// If namespace is null, xml is the old-style xml with geometry as root, so invoke XMLReader without namespace argument.
		XmlReader reader = null;
		if (ns == null) {
			reader = new XmlReader(printkeys);
		} else {
			reader = new XmlReader(printkeys, ns);
		}
		geometry = reader.getGeometry(root);
		geometry.refreshDependencies();

		return geometry;
	}


	public static VCImage XMLToImage(String xmlString) throws XmlParseException {

		return XMLToImage(xmlString, true);
	}


	static VCImage XMLToImage(String xmlString, boolean printKeys) throws XmlParseException {

		Namespace ns = Namespace.getNamespace(XMLTags.VCML_NS);

		if (xmlString == null || xmlString.length() == 0) {
			throw new XmlParseException("Invalid xml for Image: " + xmlString);
		}
		Element root = (XmlUtil.stringToXML(xmlString, null)).getRootElement();     //default parser and no validation
		Element extentElement = root.getChild(XMLTags.ExtentTag, ns);
		Element imageElement = root.getChild(XMLTags.ImageTag, ns);
//		Element extentElement = root.getChild(XMLTags.ExtentTag);
//		Element imageElement = root.getChild(XMLTags.ImageTag);
		XmlReader reader = new XmlReader(printKeys, ns);
		Extent extent = reader.getExtent(extentElement);
		VCImage vcImage = reader.getVCImage(imageElement,extent);

		vcImage.refreshDependencies();

		return vcImage;
	}


	public static MathModel XMLToMathModel(XMLSource xmlSource) throws XmlParseException {

		return XMLToMathModel(xmlSource, true);
	}


	static MathModel XMLToMathModel(XMLSource xmlSource, boolean printkeys) throws XmlParseException {

		MathModel mathModel = null;

		if (xmlSource == null){
			throw new XmlParseException("Invalid xml for Geometry.");
		}

		Document xmlDoc = xmlSource.getXmlDoc();

		// NOTES:
		//	* The root element can be <Biomodel> (old-style vcml) OR <vcml> (new-style vcml)
		//	* With the old-style vcml, the namespace was " "
		// 	* With the new-style vcml, there is an intermediate stage where the namespace for <vcml> root
		//		was set to "http://sourceforge.net/projects/VCell/version0.4" for some models and
		//		"http://sourceforge.net/projects/vcell/vcml" for some models; and the namespace for child element
		//	 	<biomdel>, etc. was " "
		// 	* The final new-style vcml has (should have) the namespace "http://sourceforge.net/projects/vcell/vcml"
		//		for <vcml> and all children elements.
		// The code below attempts to take care of this situation.
		Element root = xmlDoc.getRootElement();
		Namespace ns = null;
		if (root.getName().equals(XMLTags.VcmlRootNodeTag)) {
			// NEW WAY - with xml string containing xml declaration, vcml element, namespace, etc ...
			ns = root.getNamespace();
			Element mathRoot = root.getChild(XMLTags.MathModelTag, ns);
			if (mathRoot == null) {
				mathRoot = root.getChild(XMLTags.MathModelTag);
				//	mathRoot was null, so obtained the <Mathmodel> element with namespace " ";
				//	Re-set the namespace so that the correct XMLReader constructor is invoked.
				ns = null;
			}
			root = mathRoot;
		} 	// else - root is assumed to be old-style vcml with <Mathmodel> as root.

		// common for both new-style (with xml declaration, vcml element, etc) and old-style (mathmodel is root)
		// If namespace is null, xml is the old-style xml with mathmodel as root, so invoke XMLReader without namespace argument.
		XmlReader reader = null;
		if (ns == null) {
			reader = new XmlReader(printkeys);
		} else {
			reader = new XmlReader(printkeys, ns);
		}
		mathModel = reader.getMathModel(root);
		mathModel.refreshDependencies();

		return mathModel;
	}


	public static Simulation XMLToSim(String xmlString) throws XmlParseException {

		Simulation sim = null;
		Namespace ns = Namespace.getNamespace(XMLTags.VCML_NS);

		try {
			if (xmlString == null || xmlString.length() == 0) {
				throw new XmlParseException("Invalid xml for Simulation: " + xmlString);
			}
			Element root =  (XmlUtil.stringToXML(xmlString, null)).getRootElement();     //default parser and no validation
			Element simElement = root.getChild(XMLTags.SimulationTag, ns);
			Element mdElement = root.getChild(XMLTags.MathDescriptionTag, ns);
			Element geomElement = root.getChild(XMLTags.GeometryTag, ns);
			XmlReader reader = new XmlReader(true, ns);
			Geometry geom = null;
			if (geomElement != null) {
				geom = reader.getGeometry(geomElement);
			}
			MathDescription md = reader.getMathDescription(mdElement, geom);
			sim = reader.getSimulation(simElement, md);
		} catch (Exception pve) {
			throw new XmlParseException("Unable to parse simulation string.", pve);
		}

		sim.refreshDependencies();

		return sim;
	}


	private static final String SimulationTask_tag = "SimulationTask";
	private static final String FieldFunctionIdentifierSpec_tag = "FieldFunctionIdentifierSpec";
	private static final String ComputeResource_tag = "ComputeResource";
	private static final String TaskId_attr = "TaskId";
	private static final String JobIndex_attr = "JobIndex";
	private static final String powerUser_attr = "isPowerUser";

	public static String simTaskToXML(SimulationTask simTask) throws XmlParseException {

		String simTaskString = null;

		if (simTask == null) {
			throw new XmlParseException("Invalid input for SimulationTask: " + simTask);
		}
		Xmlproducer xmlProducer = new Xmlproducer(true);

		SimulationJob simJob = simTask.getSimulationJob();
		Simulation sim = simJob.getSimulation();

		Element container = new Element(SimulationTask_tag);
		int taskId = simTask.getTaskID();
		container.setAttribute(TaskId_attr, ""+taskId);
		int jobIndex = simJob.getJobIndex();
		container.setAttribute(JobIndex_attr, ""+jobIndex);
		boolean isPowerUser = simTask.isPowerUser();
		container.setAttribute(powerUser_attr, ""+isPowerUser);

		String computeResource = simTask.getComputeResource();
		if (computeResource!=null){
			Element computeResourceElement = new Element(ComputeResource_tag);
			Text text = new Text(computeResource);
			computeResourceElement.addContent(text);
			container.addContent(computeResourceElement);
		}

		FieldDataIdentifierSpec[] fdisSpecs = simJob.getFieldDataIdentifierSpecs();
		if (fdisSpecs!=null){
			for (FieldDataIdentifierSpec fdisSpec : fdisSpecs){
				Element fdisElement = new Element(FieldFunctionIdentifierSpec_tag);
				fdisElement.setText(fdisSpec.toCSVString());
				container.addContent(fdisElement);
			}
		}

		MathDescription md = sim.getMathDescription();
		Element mathElement = xmlProducer.getXML(md);
		container.addContent(mathElement);

		Element simElement = xmlProducer.getXML(sim);
		container.addContent(simElement);

		Geometry geom = md.getGeometry();
		if (geom != null) {
			Element geomElement = xmlProducer.getXML(geom);
			container.addContent(geomElement);
		} else {
			logger.error("No corresponding geometry for the simulation: " + sim.getName());
		}

		container = XmlUtil.setDefaultNamespace(container, Namespace.getNamespace(XMLTags.VCML_NS));
		simTaskString = XmlUtil.xmlToString(container);

		return simTaskString;
	}

	public static SimulationTask XMLToSimTask(String xmlString) throws XmlParseException, ExpressionException {

		Namespace ns = Namespace.getNamespace(XMLTags.VCML_NS);

		try {
			if (xmlString == null || xmlString.length() == 0) {
				throw new XmlParseException("Invalid xml for Simulation: " + xmlString);
			}
			Element root =  (XmlUtil.stringToXML(xmlString, null)).getRootElement();     //default parser and no validation
			if (!root.getName().equals(SimulationTask_tag)){
				throw new RuntimeException("expecting top level element to be "+SimulationTask_tag);
			}
			int taskId = Integer.parseInt(root.getAttributeValue(TaskId_attr));
			int jobIndex = Integer.parseInt(root.getAttributeValue(JobIndex_attr));
			boolean isPowerUser = false;
			if(root.getAttributeValue(powerUser_attr) != null){
				isPowerUser = Boolean.parseBoolean(root.getAttributeValue(powerUser_attr));
			}
			String computeResource = root.getChildTextTrim(ComputeResource_tag, ns);

			List<?> children = root.getChildren(FieldFunctionIdentifierSpec_tag, ns);
			ArrayList<FieldDataIdentifierSpec> fdisArrayList = new ArrayList<FieldDataIdentifierSpec>();
			for (Object child : children){
				if (child instanceof Element){
					String fdisText = ((Element)child).getTextTrim();
					FieldDataIdentifierSpec fdis = FieldDataIdentifierSpec.fromCSVString(fdisText);
					fdisArrayList.add(fdis);
				}
			}
			FieldDataIdentifierSpec[] fdisArray = fdisArrayList.toArray(new FieldDataIdentifierSpec[0]);

			Element simElement = root.getChild(XMLTags.SimulationTag, ns);
			Element mdElement = root.getChild(XMLTags.MathDescriptionTag, ns);
			Element geomElement = root.getChild(XMLTags.GeometryTag, ns);
			XmlReader reader = new XmlReader(true, ns);
			Geometry geom = null;
			if (geomElement != null) {
				geom = reader.getGeometry(geomElement);
			}
			MathDescription md = reader.getMathDescription(mdElement, geom);
			Simulation sim = reader.getSimulation(simElement, md);
			sim.refreshDependencies();

			SimulationJob simJob = new SimulationJob(sim,jobIndex,fdisArray);
			SimulationTask simTask = new SimulationTask(simJob,taskId,computeResource,isPowerUser);
			return simTask;

		} catch (Exception pve) {
			throw new XmlParseException("Unable to parse simulation string.", pve);
		}
	}

	public static String getXPathForModel() {
		return "/vcml:vcml/vcml:BioModel/vcml:Model";
	}
	public static String getXPathForSimulationSpecs() {
		return "/vcml:vcml/vcml:BioModel/vcml:SimulationSpec";
	}

	public static String getXPathForSimulationSpec(String simulationSpecID) {
		return getXPathForSimulationSpecs() + "[@Name='" + simulationSpecID + "']";
	}

	public static String getXPathForOutputFunctions(String simulationSpecID) {
		return getXPathForSimulationSpec(simulationSpecID) + "/vcml:OutputFunctions";
	}

	public static String getXPathForSpecies(String speciesID) {
		return getXPathForModel() + "/vcml:LocalizedCompound[@Name='" + speciesID + "']";
	}
	public static String getXPathForOutputFunction(String simulationSpecID, String outputFunctionID) {
		return getXPathForOutputFunctions(simulationSpecID) + "/vcml:AnnotatedFunction[@Name='" + outputFunctionID + "']";
	}

	@Deprecated
	public static String updateXML(String xml, List<Change> listOfChanges) {

		for(Change change : listOfChanges) {
			if(change instanceof ChangeAttribute) {
				XPathTarget xPathTarget = change.getTargetXPath();
				String xpath = xPathTarget.getTargetAsString();			// /sbml:sbml/sbml:model/sbml:listOfSpecies/sbml:species[@id='A']
				String newValue = ((ChangeAttribute) change).getNewValue();
				logger.trace(newValue + ", " + xpath);
				if(xpath == null || !isValidXPath(xpath)) {		// the validator only accepts species for now
					continue;
				}
				xml = updateXML(xml, xpath, newValue);
			}
		}
		return xml;
	}

	@Deprecated
	// perform a changeAttribute from within our code (not needed, the resolver does it for us)
	public static String updateXML(String xml, String xpathExpression, String newValue) {
		try {
			String filter = "sbml:species[@id='";
			String target = xpathExpression.substring(xpathExpression.lastIndexOf(filter) + filter.length());
			target = target.substring(0,target.indexOf("'"));

			//Creating document builder
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			javax.xml.parsers.DocumentBuilder builder = builderFactory.newDocumentBuilder();
			StringReader sr = new StringReader(xml);
			InputSource is = new org.xml.sax.InputSource(sr);
			org.w3c.dom.Document document = builder.parse(is);

			boolean replaced = false;
			NodeList listOfSpecies = document.getElementsByTagName("species");
			logger.trace("there are "+listOfSpecies.getLength()+" species");
			for (int i = 0; i < listOfSpecies.getLength(); i++) {
				Node species = listOfSpecies.item(i);
				if (species.getNodeType() == Node.ELEMENT_NODE) {
					String id = species.getAttributes().getNamedItem("id").getTextContent();
					logger.trace("species id: "+id);
					if(id.equals(target)) {
						species.getAttributes().getNamedItem("initialAmount").setTextContent(newValue);
						logger.trace("Changed entity " + xpathExpression);
						replaced = true;
						break;		// we updated the only species with this id, no point to continue
					}
				}
			}
			if(replaced == false ) {
				logger.error("Unable to Change entity " + xpathExpression);
			}

			StringWriter stringWriter = new StringWriter();
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(new DOMSource(document), new StreamResult(stringWriter));

			xml = stringWriter.toString();
		} catch (Exception e) {
			logger.error("unexpected exception updating attribute "+xpathExpression+" to "+newValue+": "+e.getMessage(), e);
		}
		return xml;
	}

	@Deprecated
	private static boolean isValidXPath(String xpath) {
		if(xpath.contains("listOfSpecies")) {
			return true;
		} else {
			logger.error("Only the Change of species is supported at this time");
			return false;
		}
	}

	@Deprecated
	// variant of updateXML(), never worked
	public static String updateXML2(String xml, String xpathExpression, String newValue) {
		try {
//		String xpathExpression = "/sbml:sbml/sbml:model/sbml:listOfSpecies/sbml:species[@id=\"A\"]";

			/**
			 * the following statement with xpath gives 'unexpected token' error in intellij/IDEA - not used anyway
			 * commenting it out for now.
			 *
			 * "/*:sbml/*:model/*:listOfSpecies/*:species[@id=\"A\"]"
			 * complains about the first ':' character as an unexpected token ... bug in IDE it seems.
			 */
//		xpathExpression = "/*:sbml/*:model/*:listOfSpecies/*:species[@id=\"A\"]";

			//Creating document builder
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			javax.xml.parsers.DocumentBuilder builder = builderFactory.newDocumentBuilder();
			StringReader sr = new StringReader(xml);
			InputSource is = new org.xml.sax.InputSource(sr);
			org.w3c.dom.Document document = builder.parse(is);

			//Evaluating xpath expression using Element
			XPath xpath = XPathFactory.newInstance().newXPath();

			// here follow 4 different ways to apply the change, none works
//		DTMNodeList dtmNodeList = (DTMNodeList)xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
//		Node node = dtmNodeList.item(0);
//		node.setNodeValue(newValue);

//		org.w3c.dom.Element element = (org.w3c.dom.Element)xpath.evaluate(xpathExpression, document, XPathConstants.NODE);
//		element.setTextContent(newValue);

//		Node node = (Node) xpath.compile(xpathExpression).evaluate(document, XPathConstants.NODE);

			Node node = (Node) xpath.evaluate(xpathExpression, document, XPathConstants.NODE);
			node.setNodeValue(newValue);

//		NodeList myNodeList = (NodeList) xpath.compile(xpathExpression).evaluate(document, XPathConstants.NODESET);
//		Node node = myNodeList.item(0);
//		node.setNodeValue(newValue);

			//Transformation of document to xml
			StringWriter stringWriter = new StringWriter();
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(new DOMSource(document), new StreamResult(stringWriter));

			xml = stringWriter.toString();
		} catch (Exception e) 	{
			logger.error("error while updating XML attribute: "+e.getMessage(), e);
		}
		return xml;
	}

	public static String convertXMLReservedCharacters(String input) {
		input = input.replace(">", "&gt;");
		input = input.replace("<", "&lt;");
		input = input.replace("&", "&amp;");
		input = input.replace("%", "&#37;");
//		input = input.replace("'", "&apos;");
		return input;
	}


	public static String getXPathForBioModel() {
		return "/vcml:vcml/vcml:BioModel";
	}

//public static String exportSedML(VCDocument vcDoc, int level, int version, String file) throws XmlParseException {
//	if (vcDoc == null) {
//        throw new XmlParseException("Cannot export NULL document to SEDML.");
//    }
//	if (vcDoc instanceof BioModel) {
//		SEDMLExporter sedmlExporter = new SEDMLExporter((BioModel)vcDoc, level, version);
//		return sedmlExporter.getSEDMLFile(file);
//	} else{
//		throw new RuntimeException("unsupported Document Type "+vcDoc.getClass().getName()+" for SedML export");
//	}
//}




}
