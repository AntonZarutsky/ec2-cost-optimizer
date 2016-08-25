$(document).ready(function(){
    getApplications();
});

var headers = ['Stack Name', 'Status', 'Traffic', 'Creation Time', 'Parameters'/*, 'Delete'*/];
var headersRow = generateHeader();

function getTTL(expirationDate) {
    var _MS_PER_MIN = 1000 * 60;
    var expTime = new Date(expirationDate.replace("PM", "").replace("AM", ""));
    var currTime = new Date();
    var minutes =  Math.floor((expTime - currTime) / _MS_PER_MIN);
    return minutes < 0 ? 0 : minutes
}

function getApplications() {
    jQuery.support.cors = true;

    $.ajax(
        {
            type: "GET",
            url: '/applications/all',
            data: "{}",
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            cache: false,
            success: function (data) { showApplications(data); },

            error: function (msg) {
                alert(msg.responseText);
            }
        });
}

//TODO: refactor this with templates ???

function showApplications(data) {
    var rows = headersRow + data.map(appToRows).join('');
    $('#tables').append( "<table class='table table-bordered'>" + rows + "</table>");

//    $('#header').sticky({ topSpacing: 0 });
}

function appToRows(app) {
    var ttl = getTTL(app.expirationTime);
    var cleanUpIn = ttl == 0 ? "" : " , Cleanup in " + ttl + " minutes";

    var name = wrapAsTr(
        "<td class='appName' colspan='"+ headers.length+"'>"
        + "<b>" + app.name + "</b>" + cleanUpIn + "</td>"
    );
    var data = safeRead(app.stacks, Array())
            .map(function (stack) {
                return generateRow(stack, ttl < 15 && ttl > 0);
            })
            .join('');

    return name  + data;
}

function extractParameters(stack) {
    function asString(param) {
        return "<li><b>" + param.parameterKey + '</b>:' + param.parameterValue + "</li>";
    }
    var params = safeRead(stack.parameters, Array())
        .map(asString)
        .join('');
    return "<ul>" + params + "</ul>";
}


function safeRead(data, defaultValue) {
    if (data == null || typeof data == "undefined") {
        return defaultValue;
    }
    return data;
}

function generateRow(stack, isDieSoon) {
    console.log('stack: ' + JSON.stringify(stack));
    var dieSoon = isDieSoon && stack.traffic == 0  ? "dieSoon" : undefined;

    return wrapAsTr(
        wrapAsTd(stack.name) +
        wrapAsTd(stack.status, stack.status) +
        wrapAsTd(stack.traffic, dieSoon) +
        wrapAsTd(stack.creationTime) +
        wrapAsTd(extractParameters(stack))
        //wrapAsTd(generateDelete(stack))
        , dieSoon
    );
}

function generateDelete(stack) {
   var call = "javascript:deleteStack(\""+ stack.name +"\")";
   return "<a href='" +call+"' class='glyphicon glyphicon-remove deleteAction'></a>";
}

function generateHeader() {
    function asTh(header) { return '<th>' + header + '</th>' }
    return "<tr id='header'>" + headers.map(asTh).join('') + "</tr>";
}

function wrapAsTr(content, clazz) {
    var classAtr = typeof clazz == "undefined" ? "" : "class=\"" + clazz +"\"";
    return "<tr "+ classAtr + ">" + content + "</tr>";
}

function wrapAsTd(content, clazz) {
    var classAtr = typeof clazz == "undefined" ? "" : "class=\"" + clazz +"\"";
    return "<td "+ classAtr + ">" + content + "</td>";
}

function ajaxDelete(id) {
    console.log("delete stack with id: " + id);

    $.ajax(
        {
            type: "DELETE",
            url: '/applications/' + id,
            data: "{}",
            cache: false,
            success: function (data) {
                // TODO: remove only 1 row ??
                console.log("succesfully deleted )))");
                getApplications();
            },

            error: function (msg) {
                alert(JSON.stringify(msg));
            }
        });
}

function deleteStack(id) {
    if (confirm("Are you sure?")){
        ajaxDelete(id);
    }
}
