var exec = require('cordova/exec');


var beaconHelper = {
    coolMethod: function (arg0, success, error) {
        exec(success, error, 'beaconHelper', 'coolMethod', [arg0]);
    },

    startListening: function (success, error) {
        cordova.require('cordova/channel').onCordovaReady.subscribe(function () {
            exec(success, error, 'beaconHelper', 'startListening', null);
        });
    },

    startPositioning: function (success, error) {
        cordova.require('cordova/channel').onCordovaReady.subscribe(function () {
            exec(success, error, 'beaconHelper', 'startPositioning', null);
        });
    },

    stopListening: function (success, error) {
        exec(success, error, 'beaconHelper', 'stopListening', null);
    }

}

module.exports = beaconHelper;