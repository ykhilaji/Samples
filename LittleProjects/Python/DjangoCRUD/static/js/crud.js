$(document).ready(function () {
    let inputs = $('.modal-content > .modal-body .form-group > input');

    function clearInputs() {
        inputs.each(function (index) {
            $(this).val('');
        });
    };

    $('.modal-content > .modal-footer > button').each(function (index) {
        $(this).on('click', function () {
            clearInputs();
        });
    });
});
