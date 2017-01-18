<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.5.9/angular.min.js"></script>
<script src="/resources/controller.js"></script>
<link href="/resources/eventCategory.css" rel="stylesheet"/>

<html>
<head>
    <title>Добро пожаловать</title>
</head>
<body>
<h4> Добро пожаловать! </h4>

<div class="main-app" ng-app="myApp" ng-controller="customersCtrl as $ctrl">

    <div class="main-info">
    <button ng-click="$ctrl.buttonClick();" ng-disabled = "$ctrl.buttonDisabled" >Do bet</button>
    <br>
    <br>
    Сумма ставки: <input type="number" ng-model="$ctrl.sum" maxlength="6" required>
    Id игрока: <input type="number" ng-model="$ctrl.userId" maxlength="10" required>
    </div>
    <div class="event-section" ng-repeat="event in $ctrl.languageSettings.eventInfoList" ng-if="$ctrl.languageSettings.eventMap[event.id].length">
        <div class="event-name">{{event.name}}</div>
        <div class="market-section" ng-repeat="market in $ctrl.languageSettings.eventMap[event.id]">
           <div class="market-name"> {{market.name}} </div>

            <select ng-model="$ctrl.selects[event.id]"
                    ng-options="activity.name for activity in $ctrl.languageSettings.marketMap[market.id]">
                <option value=""></option>
            </select>

        </div>
    </div>

</div>

</body>
</html>