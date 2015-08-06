/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

$(function() {
    $('#login').click(function(){
        console.log('in login click event');
        $( '#api-launcher' ).load('/most_user/login/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#logout').click(function(){
        console.log('in logout click event');
        $( '#api-launcher' ).load('/most_user/logout/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#most_user_func_1').click(function(){
        console.log('in most_user_func_1 click event');
        $( '#api-launcher' ).load('/most_user/new/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#most_user_func_2').click(function(){
        console.log('in most_user_func_2 click event');
        $( '#api-launcher' ).load('/most_user/get_user_info/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#most_user_func_3').click(function(){
        console.log('in most_user_func_3 click event');
        $( '#api-launcher' ).load('/most_user/search/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#most_user_func_5').click(function(){
        console.log('in most_user_func_5 click event');
        $( '#api-launcher' ).load('/most_user/deactivate/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});


$(function() {
    $('#most_user_func_6').click(function(){
        console.log('in most_user_func_6 click event');
        $( '#api-launcher' ).load('/most_user/activate/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});
