// app/authenticators/custom.js
import OAuth2PasswordGrant from 'ember-simple-auth/authenticators/oauth2-password-grant';
import { inject as service } from '@ember/service';
import { action } from '@ember/object';

export default class CustomAuthenticator extends OAuth2PasswordGrant {
  @service AjaxUtil;
  @service session;

  async restore(data) {
    let { auth_response } = data;
    if (auth_response) {
      return data;
    } else throw 'no valid session data';
  }

  async authenticate(mail, OTP) {
    return this.AjaxUtil.ajax('api/json/action/authenticateUser', {
      mailId: mail,
      OTP: OTP,
    }).then((response) => {
      var auth_response = {
        status: response.status,
        message: response.message,
        csrfToken: response.csrfToken,
        acc_type: response.acc_type,
        user_id: response.userId,
      };
      if (response.status != 'success') {
        throw new Error(response.message);
      } else {
        return { auth_response: auth_response };
      }
    });
  }

  async invalidate() {}
}
