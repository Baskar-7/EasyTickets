import { module, test } from 'qunit';
import { setupTest } from 'movie-ticket-booking/tests/helpers';

module('Unit | Service | AjaxUtil', function (hooks) {
  setupTest(hooks);

  // TODO: Replace this with your real tests.
  test('it exists', function (assert) {
    let service = this.owner.lookup('service:ajax-util');
    assert.ok(service);
  });
});
