import { module, test } from 'qunit';

import { setupTest } from 'movie-ticket-booking/tests/helpers';

module('Unit | Model | locations', function (hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    let store = this.owner.lookup('service:store');
    let model = store.createRecord('locations', {});
    assert.ok(model);
  });
});
