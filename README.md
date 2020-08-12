# Processing Big Data using Serverless and Java with Quarkus 

Welcome to this workshop on Quarkus for big data processing! 
We will walk through a use-case of processing and data and 
storing it into a database  using the Quarkus framework, 
and Apache OpenWhisk (IBM Cloud Functions).


## Objectives
* Build a Cloud Function to validate data and store it 
* Use OpenWhisk sequences to create atomic, composable functions
* Build a Cloud Function to retrieve our data
* Use OpenWhisk triggers to act on inserted data into our DB

## Prerequisites (optional)
Basic knowledge of Java and command line operation is required. The Skill Network Lab environment already has these things installed:
* Java
* Maven
* IBM Cloud CLI
* Code editor
* Terminal program

[Launch a Skill Network Lab environment by clicking here!](https://labs.cognitiveclass.ai/tools/theiaopenshift/lab/tree?md_instructions_url=https://cf-courses-data.s3.us.cloud-object-storage.appdomain.cloud/cloud-native-java-with-quarkus/instructions.md)
Note that the above environment has a Quarkus basic lab that you can do to get familiar with Quarkus!

# Get the source code and template project

Open a new terminal if it isn't open already:

`Terminal -> New Terminal`

Go to home directory, then clone the source code:

```
cd ~
git clone https://github.com/prpatel/ProcessingBigDataQuarkus-Workshop
```
{: codeblock}

Change into the cloned directory of the project

```
cd ProcessingBigDataQuarkus-Workshop/
```
{: codeblock}

# IBM Cloud account and login
Your presenter will provide you with a URL for creating an IBM Cloud Function account if you don't already have one.

Make sure you're logged into the ibmcloud cli! Run these commands, for the 3rd command,
select #1 option:
```
ibmcloud logout
ibmcloud login -u YOUR@EMAIL.ADDRESS
ibmcloud target --cf
```
{: codeblock}

# Create OpenWhisk package for our app
Run this command from the terminal:
```
ibmcloud fn package create bigdata
```
{: codeblock}

# Build and deploy validateAndAdd Cloud Function

In this section we'll run, build, and deploy a Cloud Function

## Run and Test locally

```
cd fn-process-comment
```
{: codeblock}

```
mvn quarkus:dev
```
{: codeblock}

Copy the contents of this file: single-line-test.json and paste it to the terminal to see the output of the program.
This is "good" input data and it will return the JSON you passed in, embedded inside a "doc" element.

Then, copy this file: single-line-test-bad.json and paste it to the terminal to see the output.
This should return an error: {"error":{"status":412,"body":"Invalid review, missing review text"}}

We've just run a postive and negative test of our application.

You can also look at the JUnit automated test case in the file:
src/test/java/org/apache/openwhisk/ProcessCommentTest.java

You can run this unit test by doing:
```
mvn test
```
{: codeblock}

> If you're wondering why this code looks different than a normal Quarkus project, 
> it is because we have built this project using Quarkus Command Line mode rather than 
> REST mode. Keeping the application as small as possible helps with two things:
> * startup time
> * runtime memory usage
> We use the special OpenWhisk-Quarkus container called `actionloop-quarkus-1.5.0:latest` 
>in the next section when we deploy it into OpenWhisk / IBM Cloud Functions. 
>
> OpenWhisk provides routing, HTTP handling, etc when you write Cloud Functions with it -
> we _can_ write it using standard REST style, but we want to be lean n' mean to get the best
> performance and response time! We have a video presentation recorded 
>[here](https://developer.ibm.com/events/lightweight-serverless-cloud-functions-with-java-and-quarkus-part-1/) 
>so you can dive into this topic if you like.  
>
## Deploy as a Cloud Function
Build the Quarkus project using mvn 
```
mvn install
```
{: codeblock}

Deploy the JAR file to IBM Cloud Functions
```
ibmcloud fn action create bigdata/processcomment target/quarkus-command-mode-1.0.0-SNAPSHOT-runner.jar --docker prpatel/actionloop-quarkus-1.5.0:latest
```
{: codeblock}

Test from command line:
```
ibmcloud fn action invoke bigdata/processcomment --result --param-file sample-input.json
```
{: codeblock}

Web enable it:
```
ibmcloud fn action update bigdata/processcomment --web true
```
{: codeblock}

Get the URL
```
ibmcloud wsk action get bigdata/processcomment --url
```
{: codeblock}



Append ".json" to the URL and put in the browser... what happens?

Look at the source code for ProcessComment.java - that will provide some answers!

(it's in src/main/java/com/ibm/openwhisk/ProcessComment.java... use the File Explorer to locate it (the icon with two files, above the search icon))

Change the error text in that file that says "Invalid review, missing review text", then rebuild and redeploy the Cloud Function:
```
mvn install
ibmcloud fn action update bigdata/processcomment target/quarkus-command-mode-1.0.0-SNAPSHOT-runner.jar --docker prpatel/actionloop-quarkus-1.5.0:latest
```
{: codeblock}

Then reload the URL you used above to see if your change is live!
 
# Create and bind a Cloudant Database
Cloudant is a NoSQL database we'll use in this workshop. 
The following steps will create and bind a Cloudant DB instance into our Cloud Functions environment.

Make sure we're in the Default space:
```
ibmcloud target -g Default
```
{: codeblock}

Create a Cloudant database server instance:
```
ibmcloud resource service-instance-create cloudant-serverless cloudantnosqldb lite us-south -p '{"legacyCredentials": false}'
```
{: codeblock}

Create Service Credentials (think of it as a username/password)
```
ibmcloud resource service-key-create creds_cloudantserverless Manager --instance-name cloudant-serverless
```
{: codeblock}

### Create and bind OpenWhisk Cloudant plugin to a package name

Bind the built-in OpenWhisk-Cloudant package into a new Cloud Functions package:
```
ibmcloud fn package bind /whisk.system/cloudant hotelreviewsdbpackage
```
{: codeblock}


### Bind the credentials to the package
Bind the Service Credentials into this package so we don't need to supply them every time we call the package:
```
ibmcloud fn service bind cloudantnosqldb hotelreviewsdbpackage --instance cloudant-serverless --keyname creds_cloudantserverless
```
{: codeblock}


### List the packages to make sure everything looks ok

```
ibmcloud fn package list
```
{: codeblock}


### See the operations available in the Cloudant OpenWhisk plugin
Want to know what pre-built operations in the OpenWhisk-Cloudant package?
```
ibmcloud fn package get --summary /whisk.system/cloudant
```
{: codeblock}


### Create Database
Now let's use this pre-built OpenWhisk-Cloudant Cloud Functions package to create our database:
```
ibmcloud fn action invoke --result hotelreviewsdbpackage/create-database -p dbname hotelreviewsdb
```
{: codeblock}

### Create default parameter with the Database Name in the package
So we don't need to provide it everytime:
```
ibmcloud fn package update hotelreviewsdbpackage -p dbname hotelreviewsdb
```
{: codeblock}

### Test that we can access the database
This should return an empty set as we haven't added anything yet... we'll do that in the next section!
```
ibmcloud fn action invoke /_/hotelreviewsdbpackage/list-documents --result
```
{: codeblock}


# Use our ProcessComment Cloud Function with the Database Functions

This creates a new sequence that executes processcomment and hotelreviewsdbpackage/write in that order, passing the result of the first to the second 
(but only if the first, or preceeding, Cloud Functions are successful and do not return an error)

```
ibmcloud fn action create bigdata/validateAndAdd --sequence bigdata/processcomment,hotelreviewsdbpackage/write
```
{: codeblock}

Now let's add some data!
```
ibmcloud fn action invoke bigdata/validateAndAdd --result --param-file sample-input.json
```
{: codeblock}

We can quickly check to see if there's anything there by running the list-document built-in function again:
`ibmcloud fn action invoke /_/hotelreviewsdbpackage/list-documents --result`

# Reading from our database

Let's read our data from the command line. You'll find a file called dbparams.json that you can use to get not just the data IDs, but the actual data:

```
ibmcloud fn action invoke /_/hotelreviewsdbpackage/list-documents --result --param-file dbparams.json 
```
{: codeblock}

How can we access this from a HTTP endpoint? We will create a new sequence using the `list-documents` along with a 
simple Cloud Function that passes it the params in that dbparams.json file.

Open up this file: fetch-comment-data/src/main/java/org/apache/openwhisk/sample/DbFetch.java 
(it's in the other project folder in this repo, fetch-comment-data) and have a look to see what we're doing.

Let's build and deploy it:

```
cd fetch-comment-data
mvn install
```
{: codeblock}

```
ibmcloud fn action create bigdata/fetchallparams target/quarkus-command-mode-1.0.0-SNAPSHOT-runner.jar --docker prpatel/actionloop-quarkus-1.5.0:latest
```
{: codeblock}

Next, let's create a sequence of this Cloud Function which sets the fetch params, and the list-documents built-in function
```
ibmcloud fn action create bigdata/fetchall --sequence bigdata/fetchallparams,hotelreviewsdbpackage/list-documents --web true
```
{: codeblock}

Try it out from the command line:

```
ibmcloud fn action invoke bigdata/fetchall --result
```
{: codeblock}


Get the URL for the Cloud Function:
```
ibmcloud fn action get bigdata/fetchall --url
```
{: codeblock}
 
 Paste it into a web browser, and don't forget to append ".json"!

We can now use this in our Web application to load in the JSON data and show it in a table.

# Extra Credit!

Have a look at the Cloudant docs and create a trigger to do something when new data 
is inserted into the database:
https://cloud.ibm.com/docs/openwhisk?topic=openwhisk-pkg_cloudant

# Next step
Congratulations on completing your lab! Please run `cleanup.sh` in a terminal to make sure you don't run out of resources in future labs.

If you are interested in continuing on this journey, you should get a [free Kubernetes cluster](https://www.ibm.com/cloud/container-service/) and your own free [IBM Container Registry](https://www.ibm.com/cloud/container-registry).

When you're ready, continue your journey with the next Quicklab!