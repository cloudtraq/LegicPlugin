// Empty constructor
function ToastyPlugin() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
ToastyPlugin.prototype.show = function(message, duration, successCallback, errorCallback) {
  let options = {};
  options.message = message;
  options.duration = duration;
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'show', [options]);
};

ToastyPlugin.prototype.initialize = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'initialize', []);
};

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

ToastyPlugin.prototype.synchronize = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'synchronize', [options]);
};

ToastyPlugin.prototype.unregister = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'unregister', [options]);
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