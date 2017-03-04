function BetRequest(bet, userId, betOutcomes) {
    this.bet = bet;
    this.userId = userId;
    this.betOutcomes = betOutcomes;
}
function BetOutcome(marketId, eventId, outcomeId, coefficient) {
    this.marketId = marketId;
    this.eventId = eventId;
    this.outcomeId = outcomeId;
    this.coefficient = coefficient;
}

var app = angular.module('myApp', []);
app.controller('customersCtrl', function ($http, $interval, $timeout) {
    var ctrl = this;
    ctrl.sum = 0;
    ctrl.userId = 0;
    ctrl.selects = {};

    ctrl.buttonDisabled = false;

    ctrl.betStatus = ["Sucessefull", "Fail"];

    $http.get("/init").then(function (response) {
        ctrl.languageSettings = response.data;
    });



    ctrl.buttonClick = function () {
        var stop;
        var timeout = $timeout(function () {
            $interval.cancel(stop);
            ctrl.buttonDisabled = false;
            alert(ctrl.betStatus[1]);
        }, 10000);

        ctrl.buttonDisabled = true;


        var betOutcomes = [];
        for (var key in ctrl.selects) {
            var betOutcome = new BetOutcome(
                ctrl.selects[key].marketId,
                key,
                ctrl.selects[key].id,
                ctrl.selects[key].coefficient
            );
            betOutcomes.push(betOutcome);
        }

        var betRequest = new BetRequest( ctrl.userId, ctrl.sum, betOutcomes);

        console.log(betRequest);
        $http.post("/dobet", betRequest).then(function (response) {
            if (response.data["active"] == true) {
                $timeout.cancel(timeout);
                alert("Your previous bet hasn't been processed yet! Try again soon.");
                ctrl.buttonDisabled = false;
            }
            else {
                stop = $interval(function () {
                    $http.get("/checkbet", {
                        params: {
                            transactionId: response.data.transactionId,
                            userId: response.data.userId
                        }
                    })
                        .then(function (response2) {
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