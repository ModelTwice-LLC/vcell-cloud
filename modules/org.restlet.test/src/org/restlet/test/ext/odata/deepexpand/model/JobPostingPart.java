/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.odata.deepexpand.model;


import java.util.List;

import org.restlet.test.ext.odata.deepexpand.model.Branch;
import org.restlet.test.ext.odata.deepexpand.model.CompanyPerson;
import org.restlet.test.ext.odata.deepexpand.model.JobPosting;
import org.restlet.test.ext.odata.deepexpand.model.JobPostingPartSpecialPayable;
import org.restlet.test.ext.odata.deepexpand.model.Location;
import org.restlet.test.ext.odata.deepexpand.model.Multilingual;

/**
* Generated by the generator tool for the OData extension for the Restlet framework.<br>
*
* @see <a href="http://praktiki.metal.ntua.gr/CoopOData/CoopOData.svc/$metadata">Metadata of the target OData service</a>
*
*/
public class JobPostingPart {

    private int durationDays;
    private int id;
    private int paidDays;
    private String siteType;
    private int startDay;
    private GeoLocation expeditionGeoLocation;
    private Tracking tracking;
    private Branch branch;
    private Multilingual description;
    private Location expeditionLocation;
    private JobPosting jobPosting;
    private CompanyPerson managingCompanyPerson;
    private List<JobPostingPartSpecialPayable> specialPayables;

    /**
     * Constructor without parameter.
     * 
     */
    public JobPostingPart() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param id
     *            The identifiant value of the entity.
     */
    public JobPostingPart(int id) {
        this();
        this.id = id;
    }

   /**
    * Returns the value of the "durationDays" attribute.
    *
    * @return The value of the "durationDays" attribute.
    */
   public int getDurationDays() {
      return durationDays;
   }
   /**
    * Returns the value of the "id" attribute.
    *
    * @return The value of the "id" attribute.
    */
   public int getId() {
      return id;
   }
   /**
    * Returns the value of the "paidDays" attribute.
    *
    * @return The value of the "paidDays" attribute.
    */
   public int getPaidDays() {
      return paidDays;
   }
   /**
    * Returns the value of the "siteType" attribute.
    *
    * @return The value of the "siteType" attribute.
    */
   public String getSiteType() {
      return siteType;
   }
   /**
    * Returns the value of the "startDay" attribute.
    *
    * @return The value of the "startDay" attribute.
    */
   public int getStartDay() {
      return startDay;
   }
   /**
    * Returns the value of the "expeditionGeoLocation" attribute.
    *
    * @return The value of the "expeditionGeoLocation" attribute.
    */
   public GeoLocation getExpeditionGeoLocation() {
      return expeditionGeoLocation;
   }
   /**
    * Returns the value of the "tracking" attribute.
    *
    * @return The value of the "tracking" attribute.
    */
   public Tracking getTracking() {
      return tracking;
   }
   /**
    * Returns the value of the "branch" attribute.
    *
    * @return The value of the "branch" attribute.
    */
   public Branch getBranch() {
      return branch;
   }
   
   /**
    * Returns the value of the "description" attribute.
    *
    * @return The value of the "description" attribute.
    */
   public Multilingual getDescription() {
      return description;
   }
   
   /**
    * Returns the value of the "expeditionLocation" attribute.
    *
    * @return The value of the "expeditionLocation" attribute.
    */
   public Location getExpeditionLocation() {
      return expeditionLocation;
   }
   
   /**
    * Returns the value of the "jobPosting" attribute.
    *
    * @return The value of the "jobPosting" attribute.
    */
   public JobPosting getJobPosting() {
      return jobPosting;
   }
   
   /**
    * Returns the value of the "managingCompanyPerson" attribute.
    *
    * @return The value of the "managingCompanyPerson" attribute.
    */
   public CompanyPerson getManagingCompanyPerson() {
      return managingCompanyPerson;
   }
   
   /**
    * Returns the value of the "specialPayables" attribute.
    *
    * @return The value of the "specialPayables" attribute.
    */
   public List<JobPostingPartSpecialPayable> getSpecialPayables() {
      return specialPayables;
   }
   
   /**
    * Sets the value of the "durationDays" attribute.
    *
    * @param durationDays
    *     The value of the "durationDays" attribute.
    */
   public void setDurationDays(int durationDays) {
      this.durationDays = durationDays;
   }
   /**
    * Sets the value of the "id" attribute.
    *
    * @param id
    *     The value of the "id" attribute.
    */
   public void setId(int id) {
      this.id = id;
   }
   /**
    * Sets the value of the "paidDays" attribute.
    *
    * @param paidDays
    *     The value of the "paidDays" attribute.
    */
   public void setPaidDays(int paidDays) {
      this.paidDays = paidDays;
   }
   /**
    * Sets the value of the "siteType" attribute.
    *
    * @param siteType
    *     The value of the "siteType" attribute.
    */
   public void setSiteType(String siteType) {
      this.siteType = siteType;
   }
   /**
    * Sets the value of the "startDay" attribute.
    *
    * @param startDay
    *     The value of the "startDay" attribute.
    */
   public void setStartDay(int startDay) {
      this.startDay = startDay;
   }
   /**
    * Sets the value of the "expeditionGeoLocation" attribute.
    *
    * @param expeditionGeoLocation
    *     The value of the "expeditionGeoLocation" attribute.
    */
   public void setExpeditionGeoLocation(GeoLocation expeditionGeoLocation) {
      this.expeditionGeoLocation = expeditionGeoLocation;
   }
   
   /**
    * Sets the value of the "tracking" attribute.
    *
    * @param tracking
    *     The value of the "tracking" attribute.
    */
   public void setTracking(Tracking tracking) {
      this.tracking = tracking;
   }
   
   /**
    * Sets the value of the "branch" attribute.
    *
    * @param branch"
    *     The value of the "branch" attribute.
    */
   public void setBranch(Branch branch) {
      this.branch = branch;
   }

   /**
    * Sets the value of the "description" attribute.
    *
    * @param description"
    *     The value of the "description" attribute.
    */
   public void setDescription(Multilingual description) {
      this.description = description;
   }

   /**
    * Sets the value of the "expeditionLocation" attribute.
    *
    * @param expeditionLocation"
    *     The value of the "expeditionLocation" attribute.
    */
   public void setExpeditionLocation(Location expeditionLocation) {
      this.expeditionLocation = expeditionLocation;
   }

   /**
    * Sets the value of the "jobPosting" attribute.
    *
    * @param jobPosting"
    *     The value of the "jobPosting" attribute.
    */
   public void setJobPosting(JobPosting jobPosting) {
      this.jobPosting = jobPosting;
   }

   /**
    * Sets the value of the "managingCompanyPerson" attribute.
    *
    * @param managingCompanyPerson"
    *     The value of the "managingCompanyPerson" attribute.
    */
   public void setManagingCompanyPerson(CompanyPerson managingCompanyPerson) {
      this.managingCompanyPerson = managingCompanyPerson;
   }

   /**
    * Sets the value of the "specialPayables" attribute.
    *
    * @param specialPayables"
    *     The value of the "specialPayables" attribute.
    */
   public void setSpecialPayables(List<JobPostingPartSpecialPayable> specialPayables) {
      this.specialPayables = specialPayables;
   }

}