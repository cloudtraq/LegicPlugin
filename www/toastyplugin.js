// Empty constructor
function ToastyPlugin() {}

ToastyPlugin.prototype.initialize = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'initialize', []);
};

ToastyPlugin.prototype.load = function(successCallback, errorCallback) {
  let _self = this;
  _self.initialize(function() {
    _self.synchronize(function() {
      _self.getFiles(function() {
        successCallback();
      }, function(err) {
        errorCallback(err);
      });
    }, function(err) {
      errorCallback(err);
    });
  }, function(err) {
    errorCallback(err);
  });
}

ToastyPlugin.prototype.startRegistration = function(deviceId, successCallback, errorCallback) {
  let options = {
    deviceId: deviceId
  };
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'initiate_registration', [options]);
};

ToastyPlugin.prototype.finishRegistration = function(deviceToken, successCallback, errorCallback) {
  let options = {
    token: deviceToken
  };
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'finish_registration', [options]);
};

ToastyPlugin.prototype.register = function(deviceId, deviceToken, successCallback, errorCallback) {
  let _self = this;
  _self.startRegistration(deviceId, function() {
    _self.finishRegistration(deviceToken, function() {
      _self.synchronize(function() {
        _self.getFiles(function() {
          successCallback();
        }, function(err) {
          errorCallback(err);
        });
      }, function(err) {
        errorCallback(err);
      })
    }, function(err) {
      errorCallback(err);
    })
  }, function(err) {
    errorCallback(err);
  });
};

ToastyPlugin.prototype.synchronize = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'synchronize', []);
};

ToastyPlugin.prototype.getFiles = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'get_files', []);
};

ToastyPlugin.prototype.getCard = function(index, successCallback, errorCallback) {
  let options = {
    index: index
  };
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'get_card', [options]);
};

ToastyPlugin.prototype.unregister = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'unregister', []);
};

// Installation constructor that binds ToastyPlugin to window
ToastyPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.toastyPlugin = new ToastyPlugin();
  return window.plugins.toastyPlugin;
};
cordova.addConstructor(ToastyPlugin.install);