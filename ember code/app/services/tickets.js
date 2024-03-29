import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';

export default class TicketsService extends Service {
  seats = [];
  ticket = [];

  @tracked tcount = 0;

  @action
  AddSeat(seat) {
    this.tcount += 1;
    this.seats.push(seat);
  }

  @action
  RemoveSeat(seat) {
    this.tcount -= 1;
    this.seats.pop(seat);
  }

  Initialize() {
    for (var i = 1; i < 5; i++) {
      var id = i;
      this.ticket[i] = { id, tic: [] };
    }
  }

  confirmTickets(id) {
    if (this.seats.length != 0) {
      this.ticket.forEach((ele1) => {
        if (ele1.id == id) {
          this.seats.forEach((ele) => {
            ele1.tic.push(ele);
          });
        }
      });
      this.seats = [];
    }
  }
}
