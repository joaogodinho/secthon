//goatConstants

var goatConstants = {
	getClasses: function() {
		return {
			categoryClass:'fa-angle-right pull-right',
			lessonCompleteClass:'glyphicon glyphicon-check lessonComplete',
			selectedMenuClass:'selected',
			keepOpenClass:'keepOpen'
		};
	},
	getServices: function() {
		return {
			lessonService: 'service/lessonmenu.mvc',
			menuService: 'service/lessonmenu.mvc',
			lessonTitleService: 'service/lessontitle.mvc',
			restartLessonService: 'service/restartlesson.mvc'	
		}
	},
	getMessages: function() {
		return {
			notFound: 'Could not find',
			noHints: 'There are no hints defined.',
			noSourcePulled: 'No source was retrieved for this lesson'
		}
	},
	getDOMContainers:function() {
		return {
			lessonMenu: '#menu-container'
		}
	}
};


