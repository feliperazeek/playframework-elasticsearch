%{
    models = [];
    for(controllerClass in play.Play.classloader.getAssignableClasses(_('controllers.ElasticSearchMashup'))) {
        resourceModel = _('controllers.ElasticSearchMashup$ObjectType').get(controllerClass)
        if(resourceModel != null) {
            models.add(resourceModel)
        }
    }
    java.util.Collections.sort(models)
}%

%{ models.eachWithIndex() { item, i -> }%
	%{
		attrs = [:]
		attrs.put('type', item)
	}%
    #{doBody vars:attrs /}
%{ } }%
