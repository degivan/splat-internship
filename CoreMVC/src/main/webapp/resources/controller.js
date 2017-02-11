function BetRequest(){
    this.bet = null;
    this.userId = null;
    this.betInfo = {};
}


var app = angular.module('myApp', []);
app.controller('customersCtrl', function ($http,$interval,$timeout) {
    var ctrl = this;
    ctrl.sum = 0;
    ctrl.userId = 0;
    ctrl.selects = {};

    ctrl.buttonDisabled = false;

    $http.get("/init").then(function (response) {
        ctrl.languageSettings = response.data;
        console.log(response.data);
    });




    ctrl.buttonClick = function(){
        var timeout = $timeout(function () {
                $interval.cancel(stop);
                alert("FAIL");
            }, 10000);

        ctrl.buttonDisabled = !ctrl.buttonDisabled;
        var betRequest = new BetRequest();
        betRequest.userId = ctrl.userId;
        betRequest.bet = ctrl.sum;
        betRequest.betInfo = ctrl.selects;

        ctrl.buttonDisabled = !ctrl.buttonDisabled;

        $http.post("/dobet", betRequest).then(function (response){
            var stop = $interval(function () {
                $http.get("/checkbet",{params: {transactionId: response.data}}).then(function (response2){
                   if (response2.data["status"] != "wait") {
                       $interval.cancel(stop);
                       $timeout.cancel(timeout);
                       alert(response2.data["status"]);
                   }
                });
            }, 1000);
        });
    }
});