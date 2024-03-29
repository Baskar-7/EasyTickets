import { module, test } from 'qunit';

import { setupTest } from 'movie-ticket-booking/tests/helpers';

module('Unit | Adapter | upcoming movies', function (hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    let adapter = this.owner.lookup('adapter:upcoming-movies');
    assert.ok(adapter);
  });
});
