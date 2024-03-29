import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import { set } from '@ember/object';

export default class NetworkErrorController extends Controller {
  @service session;
  @service router;
  @tracked interval;

  @action
  retryTransition() {
    clearInterval(this.interval);
    var icon = document.getElementById('icon');
    var attemptedTransition = this.session.attemptedTransition;
    if (attemptedTransition) {
      attemptedTransition.retry();
    } else {
      this.router.transitionTo('index');
    }
    icon.classList.toggle('fa-spin');
    var rotationInterval = setInterval(function () {
      clearInterval(rotationInterval);
      icon.classList.remove('fa-spin');
    }, 5000);
    this.interval = rotationInterval;
  }
}
