import Component from '@ember/component';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { set } from '@ember/object';

export default class MessageBoxComponent extends Component {
  @tracked msgbox;

  init() {
    super.init(...arguments);
    this.reset();
  }

  @action
  reset() {
    var msgbox = {
      show: 'false',
      message: '',
      status: '',
    };

    set(this, 'msgbox', msgbox);
  }

  didUpdateAttrs() {
    super.didUpdateAttrs();
    setTimeout(() => {
      var msgbox = {
        show: 'true',
        message: this.statusMessage.message,
        status: this.statusMessage.status,
      };
      set(this, 'msgbox', msgbox);
    }, 50);
    this.reset();
  }
}
