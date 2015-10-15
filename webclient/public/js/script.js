'use strict';

    // create the module and name it scotchApp
    	// also include ngRoute for all our routing needs
    var scotchApp = angular.module('scotchApp', ['ngResource', 'ngRoute', 'ngFileUpload', 'ui.tree'])

  .config(function($routeProvider, $locationProvider, $httpProvider) {
    //================================================
    // Check if the user is connected
    //================================================
    var checkLoggedin = function($q, $timeout, $http, $location, $rootScope){
      // Initialize a new promise
      var deferred = $q.defer();

      // Make an AJAX call to check if the user is logged in
      $http.get('/loggedin').success(function(user){
        // Authenticated
        if (user !== '0'){
          console.log("LOGIN");
          /*$timeout(deferred.resolve, 0);*/
          if($location.path() != '/')
            deferred.resolve();
          else{
            deferred.reject();
            $location.url('/info');
          }
        }
        // Not Authenticated
        else if($location.path() != '/'){
          console.log("NO LOGIN");
          //$timeout(function(){deferred.reject();}, 0);
          deferred.reject();
          $location.url('/');
        }
        else 
          deferred.resolve();

      });

      return deferred.promise;
    };
    //================================================
    
    //================================================
    // Add an interceptor for AJAX errors
    //================================================
    $httpProvider.interceptors.push(function($q, $location) {
      return {
        response: function(response) {
          // do something on success
          return response;
        },
        responseError: function(response) {
          if (response.status === 401)
            $location.url('/');
          return $q.reject(response);
        }
      };
    });
    //================================================

    //================================================
    // Define all the routes
    //================================================
    $routeProvider
      .when('/ab', {
        templateUrl: '/views/main.html'
      })
      .when('/info', {
        templateUrl : 'info.html',
        controller: 'infoController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/downloadSelfPHR', {
        templateUrl : 'downloadSelfPHR.html',
        controller: 'downloadController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/accessPermissionManagement', {
        templateUrl : 'accessPermisManage.html',
        controller: 'accessPermisController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/deleteSelfPHR', {
        templateUrl : 'deleteSelfPHR.html',
        controller: 'deleteController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/changepwd', {
        templateUrl : 'changepassword.html',
        controller: 'changePwdController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/assignPermission', {
        templateUrl : 'assignAccessPermission.html',
        controller: 'assignAccessPermissionController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/changeEmail', {
        templateUrl : 'changeEmail.html',
        controller: 'changeEmailController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/uploadPHR', {
        templateUrl : 'uploadPHR.html',
        controller: 'uploadPHRController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .when('/error',{
        templateUrl : 'error.html',
        controller  : 'errorController'
      })

      .when('/', {
        templateUrl: 'login.html',
        controller: 'loginController',
        resolve: {
          loggedin: checkLoggedin
        }
      })

      .otherwise({
        redirectTo: '/error'

      });
    //================================================

    $locationProvider.html5Mode(true);

  }) // end of config()
  .run(function($rootScope, $http){
    $rootScope.message = '';

    // Logout function is available in any pages
    $rootScope.logout = function(){
      $rootScope.message = 'Logged out.';
      $http.post('/logout');
    };
  });

    // create the controller and inject Angular's $scope
    scotchApp.controller('infoController', function($scope, $http, $location) {
        $scope.info = {};

        $http.get('/userinfo')
        .success(function(res){
            $scope.info  = res;
            console.log("INFO " + $scope.info.attribute_list);
        })
    });

    scotchApp.controller('changePwdController', function($scope, $http, $location) {
        $scope.password = {};
        $scope.password.flag = false;

        $scope.submit = function(){
            $http.post('/changepwd', {
              current_passwd        : $scope.password.curr_passwd,
              new_passwd            : $scope.password.new_passwd,
              confirm_new_passwd    : $scope.password.confirm_passwd,
              send_new_passwd_flag  : $scope.password.flag
            })
            .success(function(user){
              // No error: authentication OK
              console.log("SUCCESS");
              $location.path('/info');
            })
        };

        $scope.cancle = function(){
          $location.path('/info');
        }
    });

    scotchApp.controller('uploadPHRController', ['$scope', 'Upload' , '$http' , function ($scope, Upload, $http) {
        $scope.authorityList = {};
        $scope.attribute_all = {};
        $scope.id_node = 1;

        $scope.tree_string = "";

        $http.post('/authority_name_list')
        .success(function(res){
            $scope.authorityList = res;
        })

        $http.post('/attribute_table')
        .success(function(res){
            $scope.attribute_all = res;
            console.log($scope.attribute_all);
        })

        // upload later on form submit or something similar
        $scope.submit = function() {

          $scope.dfs($scope.tree);

          console.log("TREE STRING" + $scope.tree_string);

          /*$http.post('/set_upload', {
            tree      : $scope.tree
          })
          .success(function(result){
            if(result){
              $scope.addToNode("User", $scope.username);
            }
            else {
              console.log("NOT HAVE USER");
            }
          })*/

    /*      if ($scope.form.file.$valid && $scope.file && !$scope.file.$error) {
            console.log($scope.file);
            $scope.upload($scope.file);
          }
          else {
            console.log($scope.form.file.$valid);
          }*/
        };

      $scope.parent = ""; 

      $scope.dfs = function (node) {
        if(node){
          console.log("TEST");
          angular.forEach(node, function(value,key){
            console.log("KEY : " + key );
            console.log("Value : " + value['name'] );
            if($scope.parent == ""){
                $scope.parent = "(";
       //         console.log($scope.parent);
            }

            if(value['nodes'].length != 0){
          //    console.log(value['nodes']);           
              $scope.parent = $scope.parent + value['name'] + "+"; 
         //     console.log($scope.parent);
              $scope.dfs(value['nodes']);
            }
            else {

              if($scope.tree_string != "")
                $scope.tree_string += " or ";
              $scope.tree_string +=  $scope.parent + value['name'] + ")";
            //  console.log($scope.tree_string);
            }
            
          });

          $scope.parent = "";          
        }
      };

        // upload on file select or drop
        $scope.upload = function (file) {
            Upload.upload({
                url: 'uploadPHR',
                data: {file: file, 'username': $scope.username}
            }).then(function (resp) {
                console.log('Success ' + resp.config.data.file.name + 'uploaded. Response: ' + resp.data);
            }, function (resp) {
                console.log('Error status: ' + resp.status);
            }, function (evt) {
                var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                console.log('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
            });
        };

        // CLICK ROW
        $scope.setClickedRow = function(index){
            $scope.selectedRow = index;
            console.log($scope.selectedRow);
        }

        // CLICK NODE
        $scope.setClickedNode = function(node){
            $scope.selectedNode = node;
            $scope.selectedNode_ID = node.$modelValue.id;
            console.log($scope.selectedNode);
        }

        $scope.clickedSomewhereElse = function(){
          console.log("HIT !!")
          $scope.selectedNode = null;
          $scope.selectedRow = null;
          $scope.selectedNode_ID = null;
        };

        // TEST ui tree
      $scope.remove = function (scope) {
        scope.remove();
      };

      $scope.toggle = function (scope) {
        scope.toggle();
      };

      $scope.moveLastToTheBeginning = function () {
        var a = $scope.data.pop();
        $scope.data.splice(0, 0, a);
      };

      // ADD USERNAME TO TREE
      $scope.selectedAuthority = null;
      $scope.username = null;

      $scope.addUserToTree = function(){
        if($scope.selectedAuthority != null && $scope.username != null){
          $http.post('/check_user_exist', {
            authority_name      : $scope.selectedAuthority,
            username            : $scope.username
          })
          .success(function(result){
            if(result){
              $scope.addToNode("Username", $scope.username);
            }
            else {
              console.log("NOT HAVE USER");
            }
          })
        }
      }

      // ADD ATTRIBUTE TO TREE

      $scope.addAttribToTree = function(){
        if($scope.selectedRow != null){
          $scope.addToNode("Attribute", $scope.attribute_all[$scope.selectedRow][0]);
        }
      }

      // ADD TO TREE
      $scope.addToNode = function (type, scope) {
          var title = "";
          if($scope.selectedNode != null){

            console.log("ADD SUB NODE");
            var nodeData = $scope.selectedNode.$modelValue;
            if(type == "Username"){
              title = "User";
            }
            else
              title = type;
            nodeData.nodes.push({
              id : nodeData.id * 10 + nodeData.nodes.length,
              title: title,
              type: type,
              name: scope,
              full: type + ": " + scope,
              nodes: []
            });
          }
          else {
            console.log("ADD NODE");
            $scope.tree.push({
              id: $scope.id_node,
              title: title,

              name: scope,
              full: type + ": " + scope,
              nodes: []
            });
            console.log($scope.tree);
            $scope.id_node ++;
          }
      };

      $scope.newSubItem = function (scope) {
        var nodeData = scope.$modelValue;
        nodeData.nodes.push({
          id: nodeData.id * 10 + nodeData.nodes.length,
          title: nodeData.title + '.' + (nodeData.nodes.length + 1),
          full: id + ": " + title,
          nodes: []
        });
      };




      $scope.collapseAll = function () {
        $scope.$broadcast('collapseAll');
      };

      $scope.expandAll = function () {
        $scope.$broadcast('expandAll');
      };
      
      $scope.tree = [];

      $scope.data = [{
        'id': 1,
        'title': 'node1',
        'nodes': [
          {
            'id': 11,
            'title': 'node1.1',
            'nodes': [
              {
                'id': 111,
                'title': 'node1.1.1',
                'nodes': []
              }
            ]
          },
          {
            'id': 12,
            'title': 'node1.2',
            'nodes': []
          }
        ]
      }, {
        'id': 2,
        'title': 'node2',
        'nodrop': true, // An arbitrary property to check in custom template for nodrop-enabled
        'nodes': [
          {
            'id': 21,
            'title': 'node2.1',
            'nodes': []
          },
          {
            'id': 22,
            'title': 'node2.2',
            'nodes': []
          }
        ]
      }, {
        'id': 3,
        'title': 'node3',
        'nodes': [
          {
            'id': 31,
            'title': 'node3.1',
            'nodes': []
          }
        ]
      }];

    }]);

    scotchApp.controller('changeEmailController', function($scope, $http, $location) {
        $scope.data = {};
        $scope.info = {};

        $http.get('/userinfo')
        .success(function(res){
            $scope.info  = res;
            $scope.data.email = $scope.info.email_address;
        })

        $scope.submit = function(){
            $http.post('/change_email', {
              email                  : $scope.data.email,
              confirm_new_passwd     : $scope.data.password,
            })
            .success(function(user){
              // No error: authentication OK
              console.log("SUCCESS");
              $location.path('/info');
            })
        };

        $scope.cancle = function(){
          $location.path('/info');
        }
    });

    scotchApp.controller('contactController', function($scope, $http, $location) {
        $scope.bool = "";
        $http.get('/checklogin')
        .success(function(res){
            $scope.bool = res;
            if($scope.bool == true){
                $scope.message = 'Contact us! JK. This is just a demo';
            }
            else if($scope.bool == false)
                $location.path('/')
        })

    });


    scotchApp.controller('accessPermisController', function($scope, $http, $location) {
        $scope.access_permission_list = {};
        $scope.selectedRow = null;
        $scope.checked = {};

        $http.post('/access_permission_management_list')
        .success(function(res){
            $scope.access_permission_list = res;
            console.log(res);
        })     

        $scope.setClickedRow = function(index){
            $scope.selectedRow = index;
        }

        $scope.assign = function(){
          $location.path('/assignPermission');
        }

        $scope.edit = function(){
          console.log($scope.selectedRow);
          if($scope.selectedRow != null ){
            console.log("EDIT " + $scope.access_permission_list[$scope.selectedRow]);
            $http.post('/edit_access_permission', {
              row             : $scope.selectedRow,
              uploadflag      : $scope.access_permission_list[$scope.selectedRow][1],
              downloadflag    : $scope.access_permission_list[$scope.selectedRow][2],
              deleteflag      : $scope.access_permission_list[$scope.selectedRow][3]
            })
            .success(function(res){
              if(res == true){
                console.log("EDIT SUCCESS");
                $location.path('/accessPermissionManagement');
              }
            })
          }
        }

        $scope.delete = function(){
          console.log($scope.selectedRow);
          if($scope.selectedRow != null ){
            console.log("Delete " + $scope.access_permission_list[$scope.selectedRow]);
            $http.post('/delete_access_permission', {
              delete_user      : $scope.access_permission_list[$scope.selectedRow][0],
            })
            .success(function(res){
              if(res == true){
                console.log("Delete SUCCESS");
                 $http.post('/access_permission_management_list')
                 .success(function(res){
                     $scope.access_permission_list = res;
                     console.log(res);
                 })   
                $location.path('/accessPermissionManagement');
              }
            })
          }
        }
    });

    scotchApp.controller('assignAccessPermissionController', function($scope, $http, $location) {
        $scope.authorityList = {};
        $scope.assign = {};
        $scope.assign.uploadflag = false;
        $scope.assign.downloadflag = false;
        $scope.assign.deleteflag = false;

        $scope.hasSummit  = false;

        $http.post('/authority_name_list')
        .success(function(res){
            $scope.authorityList = res;
        })

        $scope.selectedAuthority = null;
        $scope.assign.username = null;

        $scope.submit = function(index){
          if(($scope.assign.uploadflag || $scope.assign.downloadflag || $scope.assign.deleteflag &&
            ($scope.selectedAuthority != null && $scope.assign.username != null)) && !$scope.hasSummit){
            console.log("SUCCESS");
            $scope.hasSummit = true;
            $http.post('/assign_access_permission', {
                authority : $scope.selectedAuthority,
                username : $scope.assign.username,
                uploadflag : $scope.assign.uploadflag,
                downloadflag : $scope.assign.downloadflag,
                deleteflag : $scope.assign.deleteflag,
            })
            .success(function(res){
              if(res == true){
                console.log("Assign SUCCESS");
                $location.path('/accessPermissionManagement');
              }
            })
          }
          else
            console.log("FAIL");
        }

         $scope.check = function(){
           return ($scope.assign.uploadflag || $scope.assign.downloadflag || $scope.assign.deleteflag);
         }
    });

    scotchApp.controller('downloadController', function($scope, $http, $location, $window) {
        $scope.phr_list = {};
        $scope.selectedRow = null;

        $http.post('/download_self_phr_list')
        .success(function(res){
            $scope.phr_list = res;
            console.log(res);
        })

        $scope.setClickedRow = function(index){
            $scope.selectedRow = index;
        }

        $scope.download = function(){
          $http.post('/downloadPHR', {
            index: $scope.selectedRow
          })
          .success(function(res){
                if(res){
                  console.log("DOWNLOAD FILESS !!!");
                  $window.open('/downloadPHR');
                }
          })
        }
    });

    scotchApp.controller('deleteController', function($scope, $http, $location) {
        $scope.phr_list = {};
        $scope.selectedRow = null;

        $http.post('/delete_self_phr_list')
        .success(function(res){
            $scope.phr_list = res;
            console.log("DELETE LIST : " + res);
        })

        $scope.setClickedRow = function(index){
            $scope.selectedRow = index;
        }

        $scope.download = function(){
          $http.post('/deletePHR', {
            index: $scope.selectedRow
         })
        }
    });

    scotchApp.controller('loginController', function($scope,$http,$location){
        $scope.user = {};
        $scope.check = 0;

        $scope.login = function(){
          if($scope.check == 0){
            console.log("GO LOGIN");
            $scope.check = 1;
            $http.post('/login', {
              username: $scope.user.username,
              password: $scope.user.password,
              type    : $scope.user.type
            })
            .success(function(user){
              // No error: authentication OK
              console.log("SUCCESS");
              console.log(user);
              $location.path('/info');
            })
            .error(function(){
              // Error: authentication failed
              console.log("ERROR");
              $location.path('/');
            });
          }
          else{
            console.log("WAIT LOGIN");
          }
      };
    });

    scotchApp.controller('errorController', function($scope) {
        $scope.message = "Error Don't have this page";
    });

    // CLICK ANYWHERE

scotchApp.directive('clickOff', function($parse, $document) {
    var dir = {
        compile: function($element, attr) {
          // Parse the expression to be executed
          // whenever someone clicks _off_ this element.
          var fn = $parse(attr["clickOff"]);
          return function(scope, element, attr) {
            // add a click handler to the element that
            // stops the event propagation.
            element.bind("click", function(event) {
              event.stopPropagation();
            });
            angular.element($document[0].body).bind("click",                                                                 function(event) {
                scope.$apply(function() {
                    fn(scope, {$event:event});
                });
            });
          };
        }
      };
    return dir;
});