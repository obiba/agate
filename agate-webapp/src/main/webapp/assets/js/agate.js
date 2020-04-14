/* exported agatejs */
'use strict';

var agatejs = (function() {

  const agateSignin = function(formId, onFailure) {
    $(formId).submit(function(e) {
      e.preventDefault(); // avoid to execute the actual submit of the form.
      var form = $(this);
      var url = '../ws/auth/sessions';
      var data = form.serialize(); // serializes the form's elements.

      axios.post(url, data)
        .then(() => {
          //console.dir(response);
          let redirect = '/';
          const q = new URLSearchParams(window.location.search);
          if (q.get('redirect')) {
            redirect = q.get('redirect');
          }
          window.location = redirect;
        })
        .catch(handle => {
          console.dir(handle);
          if (onFailure) {
            var banned = handle.response.data && handle.response.data.message === 'User is banned';
            onFailure(banned, handle.response.data);
          }
        });
    });
  };

  const agateSignup = function(formId, requiredFields, onFailure) {
    $(formId).submit(function(e) {
      e.preventDefault(); // avoid to execute the actual submit of the form.
      var form = $(this);
      var url = '../ws/users/_join';
      var data = form.serialize(); // serializes the form's elements.

      var formData = form.serializeArray();

      var getField = function(name) {
        var fields = formData.filter(function(field) {
          return field.name === name;
        });
        return fields.length > 0 ? fields[0] : undefined;
      };

      if (requiredFields) {
        var missingFields = [];
        requiredFields.forEach(function(item) {
          var found = formData.filter(function(field) {
            return field.name === item.name && field.value;
          }).length;
          if (found === 0) {
            missingFields.push(item.title);
          }
        });
        if (missingFields.length>0) {
          onFailure(missingFields);
          return;
        }
      }

      const realmField = getField('realm');

      axios.post(url, data)
          .then(() => {
            //console.dir(response);
            let redirect = '/';
            let values = {};
            const q = new URLSearchParams(window.location.search);
            if (q.get('redirect')) {
              redirect = q.get('redirect');
              window.location = redirect;
            } else if (realmField) {
              redirect = 'just-registered';
              values = { signin: true };
              $.redirect(redirect, values, 'GET');
            } else {
              redirect = 'just-registered';
              $.redirect(redirect, values, 'GET');
            }
          })
          .catch(handle => {
            console.dir(handle);
            if (handle.response.data.message === 'Email already in use') {
              onFailure('server.error.email-already-assigned');
            } else if (handle.response.data.message === 'Invalid reCaptcha response') {
              onFailure('server.error.bad-captcha');
            }else if (handle.response.data.messageTemplate) {
              onFailure(handle.response.data.messageTemplate);
            } else {
              onFailure('server.error.bad-request');
            }
          });
    });
  };

  const agateConfirmAndSetPassword = function(formId, onFailure) {
    $(formId).submit(function(e) {
      e.preventDefault(); // avoid to execute the actual submit of the form.
      var form = $(this);
      var url = '../ws/users/_confirm';
      var data = form.serialize(); // serializes the form's elements.

      var formData = form.serializeArray();

      if (formData[0].value.trim() === '') {
        onFailure('PasswordMissing');
        return;
      }
      if (formData[0].value.length < 8) {
        onFailure('PasswordTooShort');
        return;
      }
      if (formData[0].value !== formData[1].value) {
        onFailure('PasswordNoMatch');
        return;
      }

      axios.post(url, data)
          .then(() => {
            //console.dir(response);
            let redirect = 'just-registered';
            let values = { signin: true };
            $.redirect(redirect, values, 'GET');
          })
          .catch(handle => {
            console.dir(handle);
            onFailure('Failure', handle.response.data);
          });
    });
  };

  const agateResetPassword = function(formId, onFailure) {
    $(formId).submit(function(e) {
      e.preventDefault(); // avoid to execute the actual submit of the form.
      var form = $(this);
      var url = '../ws/users/_reset_password';
      var data = form.serialize(); // serializes the form's elements.

      var formData = form.serializeArray();

      if (formData[0].value.trim() === '') {
        onFailure('PasswordMissing');
        return;
      }
      if (formData[0].value.length < 8) {
        onFailure('PasswordTooShort');
        return;
      }
      if (formData[0].value !== formData[1].value) {
        onFailure('PasswordNoMatch');
        return;
      }

      axios.post(url, data)
          .then(() => {
            //console.dir(response);
            let redirect = '/';
            const q = new URLSearchParams(window.location.search);
            if (q.get('redirect')) {
              redirect = q.get('redirect');
            }
            window.location = redirect;
          })
          .catch(handle => {
            console.dir(handle);
            onFailure('Failure', handle.response.data);
          });
    });
  };

  const agateUpdateProfile = function(formId, requiredFields, onFailure) {
    $(formId).submit(function (e) {
      e.preventDefault(); // avoid to execute the actual submit of the form.
      var form = $(this);
      var url = '../ws/user/_current/_profile';
      var data = form.serialize();

      var formData = form.serializeArray();

      var getField = function(name) {
        var fields = formData.filter(function(field) {
          return field.name === name;
        });
        return fields.length > 0 ? fields[0] : undefined;
      };

      if (requiredFields) {
        var missingFields = [];
        requiredFields.forEach(function(item) {
          var found = formData.filter(function(field) {
            return field.name === item.name && field.value;
          }).length;
          if (found === 0) {
            missingFields.push(item.title);
          }
        });
        if (missingFields.length>0) {
          onFailure(missingFields);
          return;
        }
      }

      let language = getField('locale').value;

      axios.put(url, data)
        .then(() => {
          //console.dir(response);
          window.location = '/profile?language=' + language;
        })
        .catch(handle => {
          console.dir(handle);
          onFailure('server.error.bad-request');
        });
    });
  };

  const agateUpdatePassword = function(formId, onSuccess, onFailure) {
    $(formId).submit(function(e) {
      e.preventDefault(); // avoid to execute the actual submit of the form.
      var form = $(this);
      var url = '../ws/user/_current/password';
      var data = form.serialize(); // serializes the form's elements.

      var formData = form.serializeArray();

      if (formData[0].value.trim() === '') {
        onFailure('PasswordMissing');
        return;
      }
      if (formData[0].value.length < 8) {
        onFailure('PasswordTooShort');
        return;
      }
      if (formData[0].value !== formData[1].value) {
        onFailure('PasswordNoMatch');
        return;
      }

      axios.put(url, data)
          .then(() => {
            //console.dir(response);
            onSuccess();
          })
          .catch(handle => {
            console.dir(handle);
            onFailure('Failure', handle.response.data);
          });
    });
  };

  const agateForgotPassword = function(formId, onFailure) {
    $(formId).submit(function(e) {
      e.preventDefault(); // avoid to execute the actual submit of the form.
      var form = $(this);
      var url = '../ws/users/_forgot_password';
      var data = form.serialize(); // serializes the form's elements.

      if (decodeURI(data).trim() === 'username=') {
        return;
      }

      axios.post(url, data)
          .then(() => {
            //console.dir(response);
            $.redirect('/', {}, 'GET');
          })
          .catch(handle => {
            console.dir(handle);
            onFailure();
          });
    });
  };

  const agateSignout = function() {
    const removeAgateSession = function() {
      $.ajax({
        type: 'DELETE',
        url: '/ws/auth/session/_current'
      })
        .always(function() {
          var redirect = '/';
          $.redirect(redirect, {}, 'GET');
        });
    };

    var obibaid = Cookies.get('obibaid');
    if (obibaid) {
      console.log('Removing ' + obibaid);
      $.ajax({
        type: 'DELETE',
        url: '/ws/ticket/' + obibaid
      })
        .always(function() {
          removeAgateSession();
        });
    } else {
      removeAgateSession();
    }
  };

  const agateChangeLanguage = function(lang) {
    let key = 'language';
    let value = encodeURI(lang);
    var kvp = window.location.search.substr(1).split('&');
    var i=kvp.length;
    var x;

    while(i--) {
      x = kvp[i].split('=');
      if (x[0] === key) {
        x[1] = value;
        kvp[i] = x.join('=');
        break;
      }
    }

    if (i<0) {
      kvp[kvp.length] = [key,value].join('=');
    }

    //this will reload the page, it's likely better to store this until finished
    window.location.search = kvp.join('&');
  };

  return {
    'signin': agateSignin,
    'signout': agateSignout,
    'signup': agateSignup,
    'forgotPassword': agateForgotPassword,
    'resetPassword': agateResetPassword,
    'updatePassword': agateUpdatePassword,
    'confirmAndSetPassword': agateConfirmAndSetPassword,
    'changeLanguage': agateChangeLanguage,
    'updateProfile': agateUpdateProfile
  };
}());
