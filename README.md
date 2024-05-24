# nuxeo-labs-utils

 
## Description
This plugin contains miscellaneous utilities, mainly operations.

> [!IMPORTANT]
> This plugin is not the same as [nuxeo-labs](https://github.com/nuxeo/nuxeo-labs), which contains other utilities.

* **Operations**
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
    * Services > Labs.CreateICS

* **Automation Helpers**
  * NxLabs.getFileEXtension
  * NxLabs.getBaseName
  * NxLabs.getUserFullName
  * NxLabs.commitAndStartTransaction
  * NxLabs.threadSleep

* **Default Icon Thumbnail Factory**
  * Allows for displaying the default icon instead of calculating one

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
    * `description`: String, optional. *Important*: The description is displayed in the UI. If not passed, the operation will set the description to the viewName
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
  input = Labs.VideoGetInfo(input, {});
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
  * The operation gets the `VideoInfo` from the input blob and adds it to the `vid:transcodedVideos` schema, after getting the `VideoInfo` (width, height, format, streams, …). If `saveDoc` is true, the document is saved.
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
  * Return the mime-type of the input blob in the `nxlabs_mimetype` context variable
  * Input: `blob`
  * Output: `blob`
  * Set the `nxlabs_mimetype` context variable with the mime type of the input blob, and returns the input blob unchanged.
  * If the service cannot detect the mime type, `nxlabs_mimetype` is set to `null`.
  * ⚠️ Notice this call can be costly, the input blob temporarily duplicated on disk, etc. => we recommend using it only when you have a blob with no mime-type (which can happens sometimes after custom conversion for example)
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
  * ⚠️: In cluster mode, you will get the log of one server, you can't specify a server (this feature was built mainly for dev./trouble shooting of a single instance.)
  * ⚠️: For security reason, this operation is filtered and can only be ran by users part of the `administrators` group. If you need to change that, you must override the contribution (see `operations-contrib.xml` in `nuxeo-labs-utils-core/src/main/resources/OSGI-INF`)


* `Services > Labs.CreateICS`
  * Input: `void`
  * Return a `Blob`, an `.ics` file (mime type "text/calendar"). File name is the meeting's label + .ics
  * Parameters
    * `label`: String, required, the title of the event
    * `startDate`: required, the start of the event, with date, time and the zone. You can pass an ISO string (i.e. `2024-05-23T14:37:02.511285+02:00`)
      * If `fullDays` is `true`, you can omit the time.
    * `endDate`: Required if `duration` is not passed and `fullDays` is `false`. The end date of the event (see `startDate` about the format).
    * `duration`: Required if ` endDate`  is not passed and `fullDays` is `false`. String, a Java period, with only the hours and optionally the minutes: `"PT1H30M"`, `"PT1H"`, etc.
    * `fullDays`: Boolean, optional. When passed, the meeting is for the whole day (or days if you pass an `endDate` or a `duration`)
    * `description`: String, optional, additional information.
    * `location`: String, optional, the location (`"Room #1"` - this is not _geo_ location. Can be a link to a Zoom/Teams/etc. meeting)
    * `url`: String, optional, additional information on a website
    * `organizerMail`: String, optional, the email of the organizer
    * `attendees`: String, optional. List of mail addresses, separated with a comma
  * The operation returns a blob containing the .ics file which can then be imported to a Calendar (Outlook, Google Calendar, Apple Calendar, ...)
  * ⚠️ Little warning: Depending on the calendar tool used, some fields may not be imported, or may behave differently.
  * **Examples using JS Automation** (input `void`,  output `blob`)

```
function run(input, params) {
  // Simple event, one hour and 30 mn
  var icsBlob = Labs.CreateICS(
    null, {
      "label": "My New meeting",
      "startDate": "2024-05-28T11:00+02:00",
      "duration": "PT1H30M",
      "location": "MeetingRoom Blue"
    });
  
  // 3 days event with attendees
  var icsBlob = Labs.CreateICS(
    null, {
      "label": "My New meeting",
      "startDate": "2024-05-28",
      "duration": "PT3D",
      "location": "Somewhere",
      "attendees": "someone@abc.def, other@ghi.jkl, onemore@mno.pqr"
    });
  
  return icsBlob;

}
```



## Automation Helpers

These are helpers you can use inside Automation (regular or JS), just like the `Fn` helper. Here is an example with JS:

```
. . .

var blob = input["file:content"];
var extension = NxLabs.getFileExtension(blob.filename);
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

* `NxLabs.threadSleep`
  * Parameter is a long, the number of millisecons to pause the current thread
  * This is a wrapper for `java.lang.Thread.sleep(long millis)` 


## Default Icon Thumbnail Factory

In some context, calculating a thumbnail can be costly or can fill the log with a lot of errors, etc.

When you don't need a thumbnail for a specific document type, you can override the corresponding `ThumbnailFactory` by telling Nuxeo to use the `nuxeo.labs.utils.DocTypeIconThumbnailFactory` class instead. For example, to override the default thumbnail factory for images, you would override the `thumbnailPictureFactory`:

```
<extension target="org.nuxeo.ecm.core.api.thumbnail.ThumbnailService"
           point="thumbnailFactory">
  <thumbnailFactory name="thumbnailPictureFactory"
                    facet="Picture"
                    factoryClass="nuxeo.labs.utils.DocTypeIconThumbnailFactory" />
</extension>
```

For video, you override the `thumbnailVideoFactory`:

```
<extension target="org.nuxeo.ecm.core.api.thumbnail.ThumbnailService"
           point="thumbnailFactory">
  <thumbnailFactory name="thumbnailVideoFactory"
                    facet="Video"
                    factoryClass="nuxeo.labs.utils.DocTypeIconThumbnailFactory" />
</extension>
```

For the default factory:

```
<extension target="org.nuxeo.ecm.core.api.thumbnail.ThumbnailService"
           point="thumbnailFactory">
  <thumbnailFactory name="thumbnailDocumentFactory"
                    factoryClass="nuxeo.labs.utils.DocTypeIconThumbnailFactory" />
</extension>
```


## Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

This plugin uses a java library, [biweekly](https://github.com/mangstadt/biweekly). License is business friendly (use it as you want, with the license disclaimer, see below).


## About Nuxeo

[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.


## biweekly framework license

```
 Copyright (c) 2013-2024, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ```


