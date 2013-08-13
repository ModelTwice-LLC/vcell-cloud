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

package org.restlet.resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.engine.local.DirectoryServerResource;
import org.restlet.engine.util.AlphaNumericComparator;
import org.restlet.engine.util.AlphabeticalComparator;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

/**
 * Finder mapping a directory of local resources. Those resources have
 * representations accessed by the file system, the class loaders or other URI
 * accessible protocols. Here is some sample code illustrating how to attach a
 * directory to a router:<br>
 * 
 * <pre>
 * Directory directory = new Directory(getContext(), &quot;file:///user/data/files/&quot;);
 * Router router = new Router(getContext());
 * router.attach(&quot;/static/&quot;, directory);
 * </pre>
 * 
 * An automatic content negotiation mechanism (similar to the one in Apache HTTP
 * server) is used to select the best representation of a resource based on the
 * available variants and on the client capabilities and preferences.<br>
 * <br>
 * The directory can be used in read-only or modifiable mode. In the latter
 * case, you just need to set the "modifiable" property to true. The currently
 * supported methods are PUT and DELETE.<br>
 * <br>
 * When no index is available in a given directory, a representation can be
 * automatically generated by the
 * {@link #getIndexRepresentation(Variant, ReferenceList)} method, unless the
 * "listingAllowed" property is turned off. You can even customize the way the
 * index entries are sorted by using the {@link #setComparator(Comparator)}
 * method. The default sorting uses the friendly Alphanum algorithm based on
 * David Koelle's <a href="http://www.davekoelle.com/alphanum.html">original
 * idea</a>, using a different and faster implementation contributed by Rob
 * Heittman.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://wiki.restlet.org/docs_2.2/374-restlet.html">User Guide -
 *      Serving static files</a>
 * @author Jerome Louvel
 */
public class Directory extends Finder {

    /** The reference comparator to sort index pages. */
    private volatile Comparator<Reference> comparator;

    /**
     * Indicates if the sub-directories are deeply accessible (true by default).
     */
    private volatile boolean deeplyAccessible;

    /** The index name, without extensions (ex: "index" or "home"). */
    private volatile String indexName;

    /**
     * Indicates if the display of directory listings is allowed when no index
     * file is found.
     */
    private volatile boolean listingAllowed;

    /**
     * Indicates if modifications to local resources are allowed (false by
     * default).
     */
    private volatile boolean modifiable;

    /** Indicates if the best content is automatically negotiated. */
    private volatile boolean negotiatingContent;

    /** The absolute root reference (file, clap URI). */
    private volatile Reference rootRef;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param rootLocalReference
     *            The root URI.
     */
    public Directory(Context context, Reference rootLocalReference) {
        super(context);

        // First, let's normalize the root reference to prevent any issue with
        // relative paths inside the reference leading to listing issues.
        final String rootIdentifier = rootLocalReference.getTargetRef()
                .getIdentifier();

        if (rootIdentifier.endsWith("/")) {
            this.rootRef = new Reference(rootIdentifier);
        } else {
            // We don't take the risk of exposing directory "file:///C:/AA"
            // if only "file:///C:/A" was intended
            this.rootRef = new Reference(rootIdentifier + "/");
        }

        this.comparator = new AlphaNumericComparator();
        this.deeplyAccessible = true;
        this.indexName = "index";
        this.listingAllowed = false;
        this.modifiable = false;
        this.negotiatingContent = true;
        setTargetClass(DirectoryServerResource.class);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param rootUri
     *            The absolute root URI. <br>
     * <br>
     *            If you serve files from the file system, use file:// URIs and
     *            make sure that you register a FILE connector with your parent
     *            Component. On Windows, make sure that you add enough slash
     *            characters at the beginning, for example: file:///c:/dir/file<br>
     * <br>
     *            If you serve files from a class loader, use clap:// URIs and
     *            make sure that you register a CLAP connector with your parent
     *            Component.<br>
     * <br>
     */
    public Directory(Context context, String rootUri) {
        this(context, new Reference(rootUri));
    }

    /**
     * Returns the reference comparator used to sort index pages. The default
     * implementation used a friendly alphanum sorting.
     * 
     * @return The reference comparator.
     * @see #setAlphaNumComparator()
     */
    public Comparator<Reference> getComparator() {
        return this.comparator;
    }

    /**
     * Returns the index name, without extensions. Returns "index" by default.
     * 
     * @return The index name.
     */
    public String getIndexName() {
        return this.indexName;
    }

    /**
     * Returns an actual index representation for a given variant.
     * 
     * @param variant
     *            The selected variant.
     * @param indexContent
     *            The directory index to represent.
     * @return The actual index representation.
     */
    public Representation getIndexRepresentation(Variant variant,
            ReferenceList indexContent) {
        Representation result = null;
        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            result = indexContent.getWebRepresentation();
        } else if (variant.getMediaType().equals(MediaType.TEXT_URI_LIST)) {
            result = indexContent.getTextRepresentation();
        }
        return result;
    }

    /**
     * Returns the variant representations of a directory index. This method can
     * be subclassed in order to provide alternative representations.
     * 
     * By default it returns a simple HTML document and a textual URI list as
     * variants. Note that a new instance of the list is created for each call.
     * 
     * @param indexContent
     *            The list of references contained in the directory index.
     * @return The variant representations of a directory.
     */
    public List<Variant> getIndexVariants(ReferenceList indexContent) {
        final List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.TEXT_HTML));
        result.add(new Variant(MediaType.TEXT_URI_LIST));
        return result;
    }

    /**
     * Returns the root URI from which the relative resource URIs will be looked
     * up.
     * 
     * @return The root URI.
     */
    public Reference getRootRef() {
        return this.rootRef;
    }

    @Override
    public void handle(Request request, Response response) {
        request.getAttributes().put("org.restlet.directory", this);
        super.handle(request, response);
    }

    /**
     * Indicates if the sub-directories are deeply accessible (true by default).
     * 
     * @return True if the sub-directories are deeply accessible.
     */
    public boolean isDeeplyAccessible() {
        return this.deeplyAccessible;
    }

    /**
     * Indicates if the display of directory listings is allowed when no index
     * file is found.
     * 
     * @return True if the display of directory listings is allowed when no
     *         index file is found.
     */
    public boolean isListingAllowed() {
        return this.listingAllowed;
    }

    /**
     * Indicates if modifications to local resources (most likely files) are
     * allowed. Returns false by default.
     * 
     * @return True if modifications to local resources are allowed.
     */
    public boolean isModifiable() {
        return this.modifiable;
    }

    /**
     * Indicates if the best content is automatically negotiated. Default value
     * is true.
     * 
     * @return True if the best content is automatically negotiated.
     */
    public boolean isNegotiatingContent() {
        return this.negotiatingContent;
    }

    /**
     * Sets the reference comparator used to sort index pages.
     * 
     * @param comparator
     *            The reference comparator.
     */
    public void setComparator(Comparator<Reference> comparator) {
        this.comparator = comparator;
    }

    /**
     * Indicates if the sub-directories are deeply accessible (true by default).
     * 
     * @param deeplyAccessible
     *            True if the sub-directories are deeply accessible.
     */
    public void setDeeplyAccessible(boolean deeplyAccessible) {
        this.deeplyAccessible = deeplyAccessible;
    }

    /**
     * Sets the index name, without extensions.
     * 
     * @param indexName
     *            The index name.
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * Indicates if the display of directory listings is allowed when no index
     * file is found.
     * 
     * @param listingAllowed
     *            True if the display of directory listings is allowed when no
     *            index file is found.
     */
    public void setListingAllowed(boolean listingAllowed) {
        this.listingAllowed = listingAllowed;
    }

    /**
     * Indicates if modifications to local resources are allowed.
     * 
     * @param modifiable
     *            True if modifications to local resources are allowed.
     */
    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    /**
     * Indicates if the best content is automatically negotiated. Default value
     * is true.
     * 
     * @param negotiatingContent
     *            True if the best content is automatically negotiated.
     */
    public void setNegotiatingContent(boolean negotiatingContent) {
        this.negotiatingContent = negotiatingContent;
    }

    /**
     * Sets the root URI from which the relative resource URIs will be lookep
     * up.
     * 
     * @param rootRef
     *            The root URI.
     */
    public void setRootRef(Reference rootRef) {
        this.rootRef = rootRef;
    }

    /**
     * Sets the reference comparator based on classic alphabetical order.
     * 
     * @see #setComparator(Comparator)
     */
    public void useAlphaComparator() {
        setComparator(new AlphabeticalComparator());
    }

    /**
     * Sets the reference comparator based on the more friendly "Alphanum
     * Algorithm" created by David Koelle. The internal implementation used is
     * based on an optimized public domain implementation provided by Rob
     * Heittman from the Solertium Corporation.
     * 
     * @see <a href="http://www.davekoelle.com/alphanum.html">The original
     *      Alphanum Algorithm from David Koelle</a>
     * @see #setComparator(Comparator)
     */
    public void useAlphaNumComparator() {
        setComparator(new AlphabeticalComparator());
    }

}
