$(window).on('load', function () {
    class User {
        constructor(email, firstName, lastName, age, contacts, address) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.contacts = contacts;
            this.address = address;
        }

        static defaultUser() {
            return new User("", "", "", 0, [Contact.defaultContact()], Address.defaultAddress())
        }
    }

    class Contact {
        constructor(code, number) {
            this.code = code;
            this.number = number;
        }

        static defaultContact() {
            return new Contact("", "");
        }
    }

    class Address {
        constructor(country, city, street) {
            this.country = country;
            this.city = city;
            this.street = street;
        }

        static defaultAddress() {
            return new Address("", "", "");
        }
    }

    let method = {
        GET: "GET",
        POST: "POST",
        PUT: "PUT",
        DELETE: "DELETE",
    };

    function CrudApi(urlRoot) {
        this.urlRoot = urlRoot;
    }

    function callRPC(method, url, params, callback) {
        return $.ajax(url, {
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(params),
            method: method,
            success: callback,
            dataType: "json"
        });
    }

    CrudApi.prototype.findUsers = function (callback) {
        return callRPC(method.GET, this.urlRoot + '/api/users', {}, function (response) {
            callback(response)
        });
    };

    CrudApi.prototype.findUserByEmail = function (email, callback) {
        return callRPC(method.GET, this.urlRoot + '/api/users?email=' + email, {}, function (response) {
            callback(response)
        });
    };

    CrudApi.prototype.findUserByFirstName = function (firstName, callback) {
        return callRPC(method.GET, this.urlRoot + '/api/users/' + firstName, {}, function (response) {
            callback(response)
        });
    };

    CrudApi.prototype.updateUser = function (user, callback) {
        return callRPC(method.POST, this.urlRoot + '/api/users', user, function (response) {
            callback(response)
        });
    };

    CrudApi.prototype.insertUser = function (user, callback) {
        return callRPC(method.PUT, this.urlRoot + '/api/users', user, function (response) {
            callback(response)
        });
    };

    CrudApi.prototype.deleteUser = function (email, callback) {
        return callRPC(method.DELETE, this.urlRoot + '/api/users', {email: email}, function (response) {
            callback(response)
        });
    };

    let api = new CrudApi("http://localhost:8080");
    let templates = {};

    $("script[type='text/template']").each(function (i) {
        let name = $(this).data("name");
        let template = $(this).html();

        Mustache.parse(template);

        templates[name] = template;
    });

    function renderTemplate(templateName, dataObject) {
        return $('#' + templateName).html(Mustache.render(templates[templateName], dataObject));
    }

    $('#getByEmail').submit(function () {
        let email = $('#searchByEmail').val();
        console.log("Get user by email: " + email);

        if (email == "") {
            api.findUsers(function (response) {
                console.log(response);
                renderTemplate('userByEmailTemplate', response);
            });
        } else {
            api.findUserByEmail(email, function (response) {
                console.log(response);
                renderTemplate('userByEmailTemplate', response);
            });
        }

        return false;
    });

    $('#deleteByEmail').submit(function () {
        let email = $('#emailDelete').val();

        api.deleteUser(email, function (response) {
            console.log(response);
            renderTemplate('deleteUserByEmailTemplate', response);
        });

        return false;
    });

    $('#createUser').submit(function () {
        let inputs = $('#createUser :input');
        let user = User.defaultUser();

        inputs.each(function () {
            if (this.name == "age") {
                user[this.name] = isNaN($(this).val()) ? 0 : Number.parseInt($(this).val());
            } else {
                user[this.name] = $(this).val();
            }
        });

        console.log(user);
        console.log(JSON.stringify(user));
        api.insertUser(user, function (response) {
            console.log(response);
            renderTemplate('saveUserTemplate', response);
        });

        return false;
    });

    $("#searchByFirstNameBtn").on('click', function () {
        let firstName = $("#searchByFirstName").val();

        console.log("Find by firstname: " + firstName);
        api.findUserByFirstName(firstName, function (response) {
            console.log(response);
            renderTemplate('usersByFirstNameTemplate', response);
        });
    });
});