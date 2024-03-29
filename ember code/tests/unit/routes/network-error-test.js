import { module, test } from 'qunit';
import { setupTest } from 'movie-ticket-booking/tests/helpers';

module('Unit | Route | networkError', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:network-error');
    assert.ok(route);
  });
});
