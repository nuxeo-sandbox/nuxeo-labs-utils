# nuxeo-labs-utils

⚠️ Work In Progress ⚠️
 
## Description
This plugin contains miscellaneous utilities, mainly operations.

* Operations on Images
  * Conversion > Labs.PictureGetInfo
  * Document > Labs.AddToViews
  * Document > Labs.RemoveFromViews
  * Conversion > Labs.PictureCrop
  * Conversion > Labs.PictureRotate
* Operations on Videos
  * Conversion > Labs.VideoGetInfo
  * Document > Labs.VideoAddToTranscodedVideos
  * Document > Labs.VideoRemoveFromTranscodedVideos
* Operations on Blobs
  * Conversion > Labs.BlobGetMimeType
* Operations on Documents
  * Document > Labs.DocumentGetThumbnail
* Misc. Operations
  * Services > Labs.GetServerLog
* Automation Helpers
  * NxLabs.getFileEXtension
  * NxLabs.getBaseName
  * NxLabs.getUserFullName
  * NxLabs.commitAndStartTransaction


## Operations on Images
* `Conversion > Labs.PictureGetInfo`
  * Receives a blob as parameter, this blob must contain a Picture
  * Set the `nxlabs_ImageInfo` Context Variable with the `ImageInfo` Java structure for the input blob. It can be used as is to read the format, colorSpace, width, height and depth
  * Returns the input blob unchanged
  * (his is a simple wrapper for `org.nuxeo.ecm.platform.picture.api.ImagingService#getImageInfo`)
  * Example of use with a Javascript chain:

```
// Chain input: blob, chain output: blob
function run(input, params) {

  Labs.PictureGetInfo(input, {});
  /*
  Now, we could use
    ctx.nxlabs_ImageInfo.format (ex.: "JPEG", "PNG", ...)
    ctx.nxlabs_ImageInfo.colorSpace (ex.: "sRGB")
    ctx.nxlabs_ImageInfo.depth, ctx.nxlabs_ImageInfo.width, ctx.nxlabs_ImageInfo.height
  */
  return input;
}
```

* `Document > Labs.PictureAddToViews`
  * Add a blob to the `picture:views` field, with all the required info (width, height, format, …)
  * Input: Blob
  * Output: Document
  * Parameters
    * `document`: String, required, the id or path of the document
    * `viewName`: String, required, the name of the view. If a view of the same name already exists, it is replaced.
    * `saveDoc`: Boolean, optional, false by default
    * `description`: String, optional
    * `fileName`: String, optional. If fileName is not passed, the blob's file name is used
  * The operation gets the ImageInfo of the input blob and adds it to the `picture:views` schema, after getting the image info (width, height, format, colorSpace, depth). If `saveDoc` is true, the document is saved.
  * If a rendition of the same name already exists, it is replaced 
  * Return the document with its `picture:views` modified
  * If the document does not have the `picture` schema, the operation does nothing


* `Document > Labs.PictureRemoveFromViews`
  * Remove an entry from `picture:views`
  * Input: Document
  * Output: Document
  * Parameters
    * `viewName`: String, required, the view to remove, case insensitive
    * `saveDoc`: Boolean, optional, false by default
  * Removes the view, save the document if asked, returns the document
    * If the view is not found or if input document does not have the `picture` schema, does nothing 

* `Conversion > Labs.PictureCrop`
  * Crops the input blob, returns the cropped blob
  * Parameters
    * `left`, `top`, `width`, `height`: Integers, required 
  * Uses the default [Nuxeo `ImagingService`](https://github.com/nuxeo/nuxeo/blob/2021/modules/platform/nuxeo-platform-imaging/nuxeo-platform-imaging-core/src/main/java/org/nuxeo/ecm/platform/picture/api/ImagingService.java) and its related [CommandLine Converter contribution](https://github.com/nuxeo/nuxeo/blob/2021/modules/platform/nuxeo-platform-imaging/nuxeo-platform-imaging-core/src/main/resources/OSGI-INF/commandline-imagemagick-contrib.xml) to ImageMagick.

* `Conversion > Labs.PictureRotate`
  * Rotates the input blob, returns the rotated blob
  * Parameters
    * `angle`: Integer, required
  * Uses the default [Nuxeo `ImagingService`](https://github.com/nuxeo/nuxeo/blob/2021/modules/platform/nuxeo-platform-imaging/nuxeo-platform-imaging-core/src/main/java/org/nuxeo/ecm/platform/picture/api/ImagingService.java) and its related [CommandLine Converter contribution](https://github.com/nuxeo/nuxeo/blob/2021/modules/platform/nuxeo-platform-imaging/nuxeo-platform-imaging-core/src/main/resources/OSGI-INF/commandline-imagemagick-contrib.xml) to ImageMagick.



## Operations on Videos

* `Conversion > Labs.VideoGetInfo`
  * Set the `nxlabs_blobVideoInfo` context variable with the `VideoInfo` Java object for the input video blob
  * input: `blob`
  * Output: `blob`, the input blob, unchanged
  * Once called, info about the video is accessible via the `blobVideoInfo` Context Variable.
  * (This is a simple wrapper for `org.nuxeo.ecm.platform.video.VideoHelper#getVideoInfo`)
  * In a JS automation, for example, you could use:

```
// Chain input: blob, chain output: blob
function run(input, params) {
  Labs.VideoGetInfo(null, {});
  /*
  Now, we could use
    ctx.nxlabs_blobVideoInfo.width
    ctx.nxlabs_blobVideoInfo.height
    ctx.nxlabs_blobVideoInfo.format (a string, like "mov,mp4,m4a,3gp,3g2,mj2")
    ctx.nxlabs_blobVideoInfo.duration
    ctx.nxlabs_blobVideoInfo.frameRate
    ctx.nxlabs_blobVideoInfo.streams is an array of objects. Each stream contains (example, for first item):
      ctx.nxlabs_blobVideoInfo.streams[0].type
      ctx.nxlabs_blobVideoInfo.streams[0].codec
      ctx.nxlabs_blobVideoInfo.streams[0].streamInfo
      ctx.nxlabs_blobVideoInfo.streams[0].bitRate
  */
  return input;
}
```

* `Document > Labs.VideoAddToTranscodedVideos`
  * Add a new video rendition to the `vid:transcodedVideos` field, with all the required info (width, height, streams, …)
  * Input: `blob`, the video to add
  * Output: `document`, the modified document
  * Parameters
    * `document`: String, required, the id or path of the document
    * `renditionName`: String, required, the name of the rendition to store
    * `saveDoc`: Boolean, optional, false by default
  * The operation gets the `VideoInfo` of the input blob and adds it to the `vid:transcodedVideos` schema, after getting the `VideoInfo` (width, height, format, streams, …). If `saveDocu` is true, the document is saved.
  * If a rendition of the same name already exists (case sensitive), it is replaced.
  * Return the document with its `vid:transcodedVideos` modified
  * If the document does not have the `video` schema, or if rendition is not found, the operation does nothing

* `Document > Labs.VideoRemoveFromTranscodedVideos`
  * Remove an entry from `vid:transcodedVideos`
  * Input: `document`
  * Output: `document`
  * Parameters:
    * `renditionName`: String, required, the name of the rendition to remove
    * `saveDoc`: Boolean, optional, `false` by default
  * Remove `renditionName` (case sensitive) from vid:transcodedVideos, returns the modified input document, saved if `saveDoc` is `true`. If `renditionName` is not found or if the input document does not have the `video` schema, the operation does nothing and returns the input document unchanged.




## Operations on Blobs
* `Conversion > Labs.BlobGetMimeType`
  * Return the mimetype of the input blob in the `nxlabs_mimetype` context variable
  * Input: `blob`
  * Output: `blob`
  * Set the `nxlabs_mimetype` context variable with the mime type of the input blob, and returns the input blob unchanged.
  * (This is a simple wrapper for `org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry#getMimetypeFromBlob`)


## Operations on Documents
* `Document > Labs.DocumentGetThumbnail`
  * Return the thumbnail for the input document
  * Input: `document`
  * Output: `blob`
  * Use the `ThumbnailService` (`org.nuxeo.ecm.core.api.Blob.ThumbnailService#getThumbnail`) to return the thumbnail of the input document, whatever its type.


## Misc. Operations
* `Services > Labs.GetServerLog`
  * Input: `void`
  * Returns a `blob`, the current server.log file zipped
  * For security reason, this operation is filtered and can only be ran by users part of the `administrators` group. If you need to change that, you must override the contribution (see `operations-contrib.xml` in `nuxeo-labs-utils-core/src/main/resources/OSGI-INF`)


## Automation Helpers

These are helpers you can use inside Automation (regular or JS), just like the `Fn` helper. Here is an example with JS:

```
. . .

var blob = input["file:content"];
var extension = NxLabs.getFileEXtension(blob.filename);
var baseName = NxLabs.getBaseName(blob.filename);
input["dc:title"] = baseName;
. . .
```

* `NxLabs.getFileEXtension`
  * Parameter is a string, a full path.
  * returns a string, the file extension
  * This is a wrapper around `org.apache.commons.io.FilenameUtils#getExtension`, it hanldes null values and path with no extension:
     * foo.txt    --> "txt"
     * a/b/c.jpg  --> "jpg"
     * a/b.txt/c  --> ""
     * a/b/c      --> ""

* `NxLabs.getBaseName`
  * Parameter is a string, a full path.
  * returns a string, the base name
  * This is a wrapper around `org.apache.commons.io.FilenameUtils#getBaseName`, it hanldes null values and invalid path:
     * a/b/c.txt --> c
     * a.txt     --> a
     * a/b/c     --> c
     * a/b/c/    --> ""

* `NxLabs.getUserFullName`
  * Parameter is a string, the login of a user.
  * returns a string, the full name, firstName + " " + lastName
  * If one the value is empty, does not set the space in between
  * If both values are not set, returns the login (ex. "Administrator" in a blank new database with Nuxeo out of the box)

* `NxLabs.commitAndStartTransaction`
  * (No parametes)
  * Wrapper around the `TransationFeature` class.
  * Useful when looping and modifying several documents: databases don't like big transactions, so, for example, when looping on 10,000 documents, you want to commit the transaction every 50, 100 documents.


## Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


## About Nuxeo

[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.
