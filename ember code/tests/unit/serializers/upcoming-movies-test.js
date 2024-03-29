import { module, test } from 'qunit';

import { setupTest } from 'movie-ticket-booking/tests/helpers';

module('Unit | Serializer | upcoming movies', function (hooks) {
  setupTest(hooks);

  // Replace this with your real tests.
  test('it exists', function (assert) {
    let store = this.owner.lookup('service:store');
    let serializer = store.serializerFor('upcoming-movies');

    assert.ok(serializer);
  });

  test('it serializes records', function (assert) {
    let store = this.owner.lookup('service:store');
    let record = store.createRecord('upcoming-movies', {});

    let serializedRecord = record.serialize();

    assert.ok(serializedRecord);
  });
});
