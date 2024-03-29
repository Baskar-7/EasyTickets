import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class SeatingDetailsRoute extends Route {
  @service session;
  @service router;

  beforeModel(transistion) {
    if (!navigator.onLine) {
      this.session.set('attemptedTransition', transistion);
      this.router.transitionTo('networkError');
    } else {
      return this.session.setup().then(() => {
        if (!this.session.isAuthenticated) {
          this.session.set('attemptedTransition', transistion);
          this.session.requireAuthentication(transistion, 'index');
          this.controllerFor('index').toggleContainer(
            'open',
            'login-container'
          );
        }
      });
    }
  }

  setupController(controller, model) {
    var param = model.screen_id;
    var params = param.split('&');
    controller.initialize_details();
    if (params[0] === 'newScreen') {
      controller.set('details.theatre_id', params[1]);
      controller.getTheatreDetails();
      controller.set('details.type', 'addScreen');
    } else if (params[0] === 'updateScreenDetails') {
      controller.set('details.screen_id', params[1]);
      controller.getScreenDetails();
      controller.set('details.type', 'updateScreenDetails');
    } else {
      window.location.href = '';
      window.location.href = '/access-error';
    }
  }

  model(screen_id) {
    return screen_id;
  }
}
