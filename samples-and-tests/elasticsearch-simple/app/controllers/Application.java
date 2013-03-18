package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import controllers.elasticsearch.ElasticSearchController;

import models.*;
import models.elasticsearch.ElasticSearchSampleModel;

@ElasticSearchController.For(ElasticSearchSampleModel.class)
public class Application extends ElasticSearchController {

}