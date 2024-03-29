import Controller from '@ember/controller';
import { action } from '@ember/object';
import { tracked } from '@glimmer/tracking';
import { inject as service } from '@ember/service';
import { set } from '@ember/object';

export default class ProfileController extends Controller {
  @service router;
  @tracked profileLayoutData = {};
  @tracked activeTab = 'Profile';
  @service session;
  @service AjaxUtil;

  @action
  init() {
    super.init(...arguments);
    this.userInfo = {
      user_id: '',
      user_name: '',
      profile_pic: null,
      acc_type: '',
    };
  }

  @action
  getUserInfo() {
    var user_id = this.session.data.authenticated.auth_response.user_id;
    this.AjaxUtil.ajax('api/json/action/getUserInfo', {
      user_id: user_id,
    }).then((jsonData) => {
      var userInfo = {
        user_id: user_id,
        user_name: jsonData.fname,
        profile_pic: jsonData.profile_pic,
        acc_type: jsonData.acc_type,
      };

      this.set('userInfo', userInfo);
      //  console.log(this.userInfo.profile_pic)
    });
  }

  @action
  updateStatusMessage(jsonData) {
    var statusMessage = {
      status: jsonData.status,
      message: jsonData.message,
      show: 'true',
    };
    set(this, 'statusMessage', statusMessage);
  }

  @action
  updateProfilePic(event) {
    var image = document.getElementsByClassName('profile-image')[0];
    image.src = URL.createObjectURL(event.target.files[0]);
    const file = event.target.files[0];
    this.AjaxUtil.createFormAndSubmitFile(
      'api/json/action/updateProfilePic',
      file,
      this.userInfo.user_id
    ).then((jsonData) => {
      let data = {
        message: 'Cannot Update Profile Picture...',
        status: 'error',
      };
      if (jsonData.status == 'success') {
        data = {
          message: 'Profile Picture Updated Successfully!..',
          status: 'success',
        };
      }
      this.updateStatusMessage(data);
    });
  }

  @action
  invalidateSession() {
    //logouts the user
    this.session.invalidate();
    this.router.transitionTo('index');
  }

  @action
  setActiveTab() {
    const currentRouteName = this.router.currentRouteName;
    let activeTab;
    switch (currentRouteName) {
      case 'index':
        activeTab = 'Home';
        break;
      case 'profile.account-details':
        activeTab = 'Profile';
        break;
      case 'profile.manage-accounts':
        activeTab = 'Manage Accounts & Details';
        break;
      case 'profile.booking-history':
        activeTab = 'History';
        break;
      case 'profile.add-show':
        activeTab = 'Add Show';
        break;
      default:
        activeTab = 'Unknown';
    }

    this.set('activeTab', activeTab);
  }
}
