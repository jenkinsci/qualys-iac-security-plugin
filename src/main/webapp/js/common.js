var action = document.querySelector('form.jenkins-form') ? document.querySelector('form.jenkins-form').action : undefined;
function setPageStateValue() {
    let inputList = document.querySelectorAll('.isPageLoad');
    inputList.forEach(function (input, i) {
        if (input) {
            input.value = 'false';
        }
    });
}
function checkAnyValidation() {
    if (document.querySelector('.yui-submit-button button')) {
        if (document.querySelectorAll('#qualys-configuration-body .validation-error-area--visible .error').length > 0) {
            document.querySelector('.yui-submit-button button').disabled = true;
        } else {
            document.querySelector('.yui-submit-button button').disabled = false;
        }
    }
}
function triggerValidation(ev) {
    triggerValidationById(ev.target.id);
}
function triggerValidationById(id) {
    if (document.getElementById(id)) {
        document.getElementById(id).dispatchEvent(new Event('change', {'bubbles': true}));
    }
}
function scrollToInvalidInput(event) {
    //debugger;
    if (document.querySelectorAll('#qualys-configuration-body .validation-error-area--visible .error').length > 0) {
        event.preventDefault();
        event.stopPropagation();
        document.getElementById('qualys-configuration-body').scrollIntoView({
            behavior: 'smooth',
            block: 'nearest',
            inline: 'start'
        });
    }
}
function checkIaCEndPointSelected() {
    if (document.getElementById("selectIaCServiceEndpoint")) {
        if (document.getElementById("selectIaCServiceEndpoint").value === '-1') {
            var element = document.getElementById("selectIaCServiceEndpoint").parentElement.parentElement.querySelector('.validation-error-area');
            element.innerHTML = '<div class="error">Select IaC Scan service/server endpoint</div>';
            element.classList.add("validation-error-area--visible");
            element.style.height = '21px';
        } else {
            var element = document.getElementById("selectIaCServiceEndpoint").parentElement.parentElement.querySelector('.validation-error-area');
            element.innerHTML = '';
            element.classList.remove("validation-error-area--visible");
            element.style.height = '0px';
        }
    }
}
function triggerAllValidation(event) {
    let inputList = document.querySelectorAll('#qualys-configuration-body input');
    inputList.forEach(function (input, i) {
        if (input) {
            triggerValidationById(input.id);
        }
    });
    checkIaCEndPointSelected();
    setTimeout(scrollToInvalidInput(event), 3000);
}
function assignAsteriskMark() {
    let labelList = document.querySelectorAll('#qualys-configuration-body .required .jenkins-form-label');
    labelList.forEach(function (element, i) {
        var text = element.innerText || element.textContent;
        element.innerHTML = text + '<sup style="font-size:18px;font-weight:600;color:red">*</sup>';
    });
}
function registerClickEvent() {
    if (document.querySelector('.apply-button button')) {
        document.querySelector('.apply-button button').onclick = triggerAllValidation;
        document.querySelector('.yui-submit-button button').onclick = triggerAllValidation;
    }
    if (document.querySelector('#qualys-configuration-body .repeatable-add')) {
        document.querySelector('#qualys-configuration-body .repeatable-add').onclick = assignAsteriskMark;
    }

    document.addEventListener('click', function (event) {
        //debugger;
        if (event.target.textContent.trim().toLowerCase() == 'save' || event.target.textContent.trim().toLowerCase() == 'apply') {
            triggerAllValidation(event);
        }
    });
}
function createUUID() {
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}
function assignUniqueId() {
    let inputList = document.querySelectorAll('#qualys-configuration-body input');
    inputList.forEach(function (input, i) {
        if (!input.id) {
            input.id = createUUID();
        }
    });
}
setTimeout(registerClickEvent, 500);
setTimeout(setPageStateValue, 500);
setInterval(assignUniqueId, 200);
setTimeout(assignAsteriskMark, 200);
setInterval(checkAnyValidation, 200);


