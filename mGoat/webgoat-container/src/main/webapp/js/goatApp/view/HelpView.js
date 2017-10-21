define(['jquery',
	'underscore',
	'backbone'],
function($,
	_,
	Backbone) {
	return Backbone.View.extend({
		el:'#lessonHelpWrapper .lessonHelp.lessonPlan', //Check this
		initialize: function() {
		},
		render:function(title) {
			
		},
		onModelLoaded: function() {
			this.trigger(this.loadedMessage,this.helpElement);
		}
	});
});