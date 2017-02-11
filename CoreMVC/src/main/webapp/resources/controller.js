function BetRequest(){
    this.bet = null;
    this.userId = null;
    this.betOutcomes = [{}];
}


var app = angular.module('myApp', []);
app.controller('customersCtrl', function ($http,$interval,$timeout,$window) {
    var ctrl = this;
    ctrl.sum = 0;
    ctrl.userId = 0;
    ctrl.selects = {};

    ctrl.buttonDisabled = false;

    $http.get("/init").then(function (response) {
        ctrl.languageSettings = response.data;
        $window.console.log(ctrl.languageSettings);
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

        var i=0;
        for (var key in ctrl.selects)
        {
            betRequest.betOutcomes[i].marketId = ctrl.selects[key].marketId;
            betRequest.betOutcomes[i].outcomeId = ctrl.selects[key].id;
            betRequest.betOutcomes[i].eventId = key;
            betRequest.betOutcomes[i].coefficient = ctrl.selects[key].coefficient;
            i++;
        }

        ctrl.buttonDisabled = !ctrl.buttonDisabled;


        $http.post("/dobet", betRequest).then(function (response){
            var stop = $interval(function () {
                $http.get("/checkbet",{params: {transactionId: response.data.transactionId, userId:response.data.userId}})
                    .then(function (response2){
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