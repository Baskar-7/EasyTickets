import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';

export default class NetworkErrorRoute extends Route {
  @service router;
  @service session;

  beforeModel() {
    if (navigator.onLine) {
      var attemptedTransition = this.session.attemptedTransition;
      if (attemptedTransition) {
        attemptedTransition.retry();
      } else {
        this.router.transitionTo('index');
      }
    }
  }
}
