$(document).ready(function () {
    let inputs = $('.modal-content > .modal-body .form-group > input');
    let inputsMap = {};

    inputs.each(function (index) {
        let key = $(this).attr('name');

        inputsMap[key] = $(this);
    });

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

    $('tr').on('click', function () {
        let id = $(this).data("id");
        let elements = $(this).find('td');

        inputsMap['id'].val(id);

        elements.each(function (index) {
            let inputName = $(this).data('input-name');
            let value = $(this).text();

            inputsMap[inputName].val(value)
        });
    });
});
