function BetRequest(){
    this.bet = null;
    this.userId = null;
    this.betOutcomes = [{}];
}


var app = angular.module('myApp', []);
app.controller('customersCtrl', function ($http,$interval,$timeout) {
    var ctrl = this;
    ctrl.sum = 0;
    ctrl.userId = 0;
    ctrl.selects = {};

    ctrl.buttonDisabled = false;

    ctrl.betStatus = [];
    ctrl.betStatus[0] = "Sucessefull";
    ctrl.betStatus[1] = "Fail";

    $http.get("/init").then(function (response) {
        ctrl.languageSettings = response.data;
    });




    ctrl.buttonClick = function(){
        var stop;
        var timeout = $timeout(function () {
                $interval.cancel(stop);
                ctrl.buttonDisabled = false;
                alert(ctrl.betStatus[1]);
            }, 10000);

        ctrl.buttonDisabled = true;
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


        $http.post("/dobet", betRequest).then(function (response){
            
            if (response.data["active"] == true) {
                $timeout.cancel(timeout);
                alert("Your previous bet hasn't been processed yet! Try again soon.");
                ctrl.buttonDisabled = false;
            }
            else {
                stop = $interval(function () {
                    $http.get("/checkbet",{params: {transactionId: response.data.transactionId, userId:response.data.userId}})
                        .then(function (response2){
                            if (response2.data != 2) {
                                $interval.cancel(stop);
                                $timeout.cancel(timeout);
                                ctrl.buttonDisabled = false;
                                alert(ctrl.betStatus[response2.data]);
                            }
                        });
                }, 1000);
            }

        });
    }
});