# Play Framework - Elastic Search Module
by Felipe Oliveira
http://geeks.aretotally.in
http://twitter.com/_felipera

Integrate Elastic Search in a Play! Framework Application. This module uses JPA events to notify Elastic Search of events of their own. In Local Mode it embeds a running Elastic Search instance (port 9002 by default).


## Prerequisites

Play! 1.1



### Install the module

Install the elasticsearch module from the modules repository:

`
play install elasticsearch 
`


### Enable the module

After installing the module, add the following to your application.conf to enable it:

`
module.elasticsearch=${play.path}/modules/elasticsearch-0.0.1
`


### Configure the module

You need to configure the module by setting these properties in your application.conf:

# Elastic Search
elasticsearch.local=true
elasticsearch.client=false
elasticsearch.models=models.Post

Local Model is the only supported at this point which embeds an Elastic Search instance with Play!. Elastic Search runs on port 9002. 
Currently you need to define the models that will be part of Elastic Search, there's an improvement coming up soon where you won't need to do that anymore (convention over configuration right?!).


All of the properties are required.



## Usage

You basically need to extend ElasticSearchModel instead of Model on your as you can guess model class. It only works for JPA so far. 



## User Interface 

After you start your application (play run), you should have an admin interface automatically running on http://localhost:9002/es-admin/.



## Source Code

Fork it on Github https://github.com/feliperazeek/playframework-linkedin.


## Roadmap

-> Add support to external instances using TransportClient.
-> Add different methods of notification (ElasticSearch River, AMQP, JMS)
-> Adding support for non-Jpa models. 



## Credits

-> Shay Banon for the great work with Elastic Search and Compass, I have been following his work for a few years, great stuff.
-> Ben Birch (https://github.com/mobz) for the work on the User Interface.
