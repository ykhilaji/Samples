$(window).on('load', function () {
    let templates = {};

    $('script[type=x-tmpl-mustache]').each(function (index) {
        let template = $(this).html();
        let templateId = $(this).attr('id');

        Mustache.parse(template);

        templates[templateId] = template;
    });

    $('#if-else-render-btn').on('click', function () {
        console.log($(this).data('template'));
        console.log($('#if-else-render-input').val());
        let rendered = Mustache.render(templates[$(this).data('template')], {"condition": $('#if-else-render-input').val() > 10, "value": $('#if-else-render-input').val()});
        $('#' + $(this).data('target')).html(rendered);
    });

    $('#loop-target-btn').on('click', function () {
        console.log($(this).data('template'));
        let rendered = Mustache.render(templates[$(this).data('template')], {"array": [
            1,2,3,4,5,6,7,8,9,10
        ]});
        $('#' + $(this).data('target')).html(rendered);
    });

    $('#with-function-loop-target-btn').on('click', function () {
        console.log($(this).data('template'));
        let rendered = Mustache.render(templates[$(this).data('template')], {"array": [
            1,2,3,4,5,6,7,8,9,10
        ], "toString": function () {
            return this + 100;
        }});
        $('#' + $(this).data('target')).html(rendered);
    });

    $('#object-btn').on('click', function () {
        console.log($(this).data('template'));
        let rendered = Mustache.render(templates[$(this).data('template')], {"object": {
            "id": 1,
            "valueOne": "someValue",
            "valueTwo": "someAnotherValue"
        }});
        $('#' + $(this).data('target')).html(rendered);
    });
});