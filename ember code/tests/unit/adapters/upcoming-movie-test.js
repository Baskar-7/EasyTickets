import { module, test } from 'qunit';

import { setupTest } from 'movie-ticket-booking/tests/helpers';

module('Unit | Adapter | upcoming movie', function (hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    let adapter = this.owner.lookup('adapter:upcoming-movie');
    assert.ok(adapter);
  });
});
