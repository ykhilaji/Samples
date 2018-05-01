const mongoose = require('mongoose');
const Schema = mongoose.Schema;

mongoose.connect('mongodb://192.168.99.100:27017/sample', {
    autoReconnect: true,
    reconnectTries: 5,
    reconnectInterval: 500,
    poolSize: 10
}, function () {
    console.log("Connected");
});

const UserSchema = new Schema({
    _id: Schema.Types.ObjectId,
    firstName: {type: String, required: true},
    lastName: {type: String, required: false, default: ""},
    email: {type: String, required: true, unique: true, index: true},
    age: {type: Number, required: true},
    birth: {type: Date, required: false},
    address: {type: Schema.Types.ObjectId, ref: 'Address'},
    contacts: [{type: Schema.Types.ObjectId, ref: 'Contact'}],
    created: Date
});

const AddressSchema = new Schema({
    user: {type: Schema.Types.ObjectId, ref: 'User'},
    country: {type: String, required: true, unique: true},
    city: {type: String, required: true},
    street: {type: String, required: true},
    house: {type: Number, required: true},
}, {timestamps: {createdAt: 'created', updatedAt: 'updated'}});

const ContactSchema = new Schema({
    user: {type: Schema.Types.ObjectId, ref: 'User'},
    code: {type: String, required: true, unique: true},
    number: {type: String, required: true},
    created: Date
});

AddressSchema.index({country: 1, city: 1, street: 1});
AddressSchema.virtual("full").get(function () {
    return `${this.country} ${this.city}:${this.street}`;
});

UserSchema.index({firstName: 1, lastName: 1});
UserSchema.virtual("fullName").get(function () {
    return `${this.firstName} ${this.lastName}`;
});
UserSchema.set('toJSON', {getters: true, virtuals: false});
UserSchema.set('toObject', {getters: false}); // getters true -> virtuals true

ContactSchema.virtual("full").get(function () {
    return `${this.code}:${this.number}`;
});

UserSchema.pre('save', function (next) {
    this.created = new Date();
    next();
});

ContactSchema.pre('save', function (next) {
    this.created = new Date();
    next();
});

if (!UserSchema.options.toObject) UserSchema.options.toObject = {};
if (!AddressSchema.options.toObject) AddressSchema.options.toObject = {};
if (!ContactSchema.options.toObject) ContactSchema.options.toObject = {};

UserSchema.options.toObject.transform = function (doc, ret, options) {
    delete ret._id;
    delete ret.__v;
    return ret;
};

AddressSchema.options.toObject.transform = function (doc, ret, options) {
    delete ret._id;
    delete ret.__v;
    return ret;
};

ContactSchema.options.toObject.transform = function (doc, ret, options) {
    delete ret._id;
    delete ret.__v;
    return ret;
};

const User = mongoose.model('User', UserSchema);
const Address = mongoose.model('Address', AddressSchema);
const Contact = mongoose.model('Contact', ContactSchema);

User.remove({}, function (err) {
    console.log("Removed all users");
});

Address.remove({}, function (err) {
    console.log("Removed all addresses");
});

Contact.remove({}, function (err) {
    console.log("Removed all contacts");
});


setTimeout(function () {
    let user = new User({
        _id: new mongoose.Types.ObjectId(),
        firstName: "UserFirstName",
        lastName: "UserLastName",
        email: "user@user.com",
        age: 10,
        birth: new Date(2008, 8, 8)
    });

    user.save(function (err) {
        let address = new Address({
            user: user._id,
            country: "SomeCountry",
            city: "SomeCity",
            street: "SomeStreet",
            house: 1
        });

        address.save(function (err) {
            console.log("Address saved");

            User.findByIdAndUpdate(user._id, {"$set": {address: address._id}}, function (err, _) {
                console.log("Update user's address");
            });
        });

        let contact1 = new Contact({
            user: user._id,
            code: "000",
            number: "111-222-333"
        });

        let contact2 = new Contact({
            user: user._id,
            code: "555",
            number: "333-555-777"
        });

        contact1.save(function (err) {
            console.log("Contact saved");

            User.findByIdAndUpdate(user._id, {"$push": {contacts: contact1._id}}, function (err, _) {
                console.log("Update user's contacts list");
            });
        });

        contact2.save(function (err) {
            console.log("Contact saved");

            User.findByIdAndUpdate(user._id, {"$push": {contacts: contact2._id}}, function (err, _) {
                console.log("Update user's contacts list");
            });
        });

        console.log("User saved");
    });
}, 500);


setTimeout(function () {
    User.findOne({firstName: "UserFirstName"}, 'email')
        .populate('address', 'country city')
        .populate('contacts', 'number')
        .exec(function (err, user) {
            console.log(user);

            // { contacts: [ { number: '111-222-333' }, { number: '333-555-777' } ],
            //     email: 'user@user.com',
            //     address: { country: 'SomeCountry', city: 'SomeCity' } }
        });

    User.findOne({firstName: "UserFirstName"}, 'firstName age')
        .where('age').gt(5).lt(15)
        .exec(function (err, user) {
            console.log(user);

            // { firstName: 'UserFirstName', age: 10 }
        });

    Address.findOne({country: "SomeCountry"}, 'city street')
        .populate('user', 'firstName lastName')
        .exec(function (err, address) {
            console.log(address);

            // { user: { lastName: 'UserLastName', firstName: 'UserFirstName' },
            //     city: 'SomeCity',
            //         street: 'SomeStreet' }
        });

    User.findOne({firstName: "UserFirstName"}, function (err, user) {
        Contact.find({user: user._id})
            .where('code').eq('000')
            .limit(1)
            .sort('-code')
            .select('number')
            .exec(function (err, contact) {
                console.log(contact);

                // [ { number: '111-222-333' } ]
            });
    });

    Contact.find({}, 'code number', function (err, contacts) {
        console.log(contacts);

        // [ { code: '000', number: '111-222-333' },
        //     { code: '555', number: '333-555-777' } ]
    });
}, 1000);


setTimeout(function () {
    mongoose.disconnect(function (err) {
        console.log("Disconnected");
    });
}, 1500);