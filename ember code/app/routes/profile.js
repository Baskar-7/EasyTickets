import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class ProfileRoute extends Route {
  @service session;
  @service router;

  /*
  setup the session and then checks if the user is authenticated or not using isAuthenticated varaible in the session service and if not create 
  variable attemptedtransition to store the transition and then route to index page opens the logincontainer using the controllerfor
*/

  beforeModel(transistion) {
    if (!navigator.onLine) {
      this.session.set('attemptedTransition', transistion);
      this.router.transitionTo('networkError');
    }
    return this.session.setup().then(() => {
      if (!this.session.isAuthenticated) {
        this.session.set('attemptedTransition', transistion);
        this.session.requireAuthentication(transistion, 'index');
        this.controllerFor('index').toggleContainer('open', 'login-container');
      }
    });
  }

  setupController(controller, model) {
    controller.getUserInfo();
  }
}
