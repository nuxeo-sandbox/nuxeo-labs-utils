# nuxeo-labs-utils

⚠️ Work In Progress ⚠️
 
## Description
This plugin contains miscellaneous utilities, mainly operations.


## List of Operations

### Operations on Images
* `Conversion > Labs.PictureGetInfo`
  * Receives a blob as parameter, this blob must contain a Picture
  * Set a Context Variable (see below) with the `ImageInfo` Java structure for the input blob. It can be used as is to read the format, colorSpace, width, height and depth
  * Returns the input blob unchanged
  * Parameter: `varName`, string, Optional. The name of a Context Variable to fill with the value. If not passed, fills the `nxlabs_ImageInfo` Context Variable with the `ImageInfo`.
  * Example of use with a Javascript chain:

```
// Chain input: blob
// Chain output: blob
function run(input, params) {
  
  // No parameter passed, get the info in nxlabs_ImageInfo
  Labs.PictureGetInfo(null, {});
  /*
  Now, we could use
    ctx.nxlabs_ImageInfo.format (ex.: "JPEG", "PNG", ...)
    ctx.nxlabs_ImageInfo.colorSpace (ex.: "sRGB")
    ctx.nxlabs_ImageInfo.depth, ctx.nxlabs_ImageInfo.width, ctx.nxlabs_ImageInfo.height
  */
  
  return input;

}
```


### Operations on Videos

### Misc. Operations
* `Services > Labs.GetServerLog`
  * Input: `void`
  * Returns a `blob`, the current server.log file zipped
  * For security reason, this operation is filtered and can only be ran by users part of the `administrators` group. If you need to change that, you must override the contribution (see operations-contrib.xml in nuxeo-labs-utils-core/src/main/resources/OSGI-INF)




# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


# About Nuxeo

[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.
