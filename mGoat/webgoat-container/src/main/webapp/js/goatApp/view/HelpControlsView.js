define(['jquery',
	'underscore',
	'backbone'],
function($,_,Backbone) {
	return Backbone.View.extend({
		el:'#help-controls', //Check this

		initialize: function (options) {
			if (!options) {
				return;
			}
			this.hasPlan = options.hasPlan;
		},
		    
		render:function(title) {
			//this.$el.html();
			// if still showing, hide
			$('#show-plan-button').hide();

			if (this.hasPlan) {
				this.$el.find('#show-plan-button').unbind().on('click',_.bind(this.showPlan,this)).show();
			}
			
			this.$el.find('#restart-lesson-button').unbind().on('click',_.bind(this.restartLesson,this)).show();
			//this.$el.append(this.helpButtons.restartLesson);
		},
		showPlan: function() {
			this.trigger('plan:show','plan');
		},
		restartLesson: function() {
			this.trigger('lesson:restart');
		}
	});
});