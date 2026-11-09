const assert = require('node:assert/strict');
const { after, before, test } = require('node:test');
const { createApp } = require('./index');

const users = [];
const collection = {
    findOne: async (query) => users.find((user) =>
        Object.entries(query).every(([key, value]) => user[key] === value)
    ) || null,
    insertOne: async (user) => users.push(user),
    find: () => ({ toArray: async () => users }),
    deleteOne: async ({ email }) => {
        const index = users.findIndex((user) => user.email === email);
        if (index >= 0) users.splice(index, 1);
    },
    deleteMany: async () => users.splice(0),
    updateOne: async ({ email }, { $set }) => {
        const user = users.find((entry) => entry.email === email);
        if (user) Object.assign(user, $set);
    }
};

let server;
let baseUrl;

before(async () => {
    users.splice(0);
    server = createApp(collection).listen(0, '127.0.0.1');
    await new Promise((resolve) => server.once('listening', resolve));
    baseUrl = `http://127.0.0.1:${server.address().port}`;
});

after(async () => {
    await new Promise((resolve, reject) => {
        server.close((error) => error ? reject(error) : resolve());
    });
});

test('supports the Android CRUD API request and response shapes', async () => {
    const user = {
        date: '1762672800',
        name: 'Test User',
        email: 'test@example.com',
        password: 'example'
    };

    let response = await fetch(`${baseUrl}/insert`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(user)
    });
    assert.equal(response.status, 200);

    response = await fetch(`${baseUrl}/insert`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(user)
    });
    assert.equal(response.status, 400);

    response = await fetch(`${baseUrl}/selectuser`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ email: user.email, password: user.password })
    });
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), user);

    response = await fetch(`${baseUrl}/selectall`);
    assert.equal(response.status, 200);
    assert.equal((await response.json()).length, 1);

    response = await fetch(`${baseUrl}/update`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ email: user.email, name: 'Updated', password: user.password })
    });
    assert.equal(response.status, 200);
    assert.equal(users[0].name, 'Updated');

    response = await fetch(`${baseUrl}/deleteuser`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ email: user.email })
    });
    assert.equal(response.status, 200);
    assert.equal(users.length, 0);

    response = await fetch(`${baseUrl}/checkexists`);
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), []);

    response = await fetch(`${baseUrl}/insert`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(user)
    });
    assert.equal(response.status, 200);

    response = await fetch(`${baseUrl}/deleteall`);
    assert.equal(response.status, 200);
    assert.equal(users.length, 0);
});
