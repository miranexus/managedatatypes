require('dotenv').config();

const express = require('express');
const { MongoClient } = require('mongodb');

// Keep these routes and response codes aligned with InterfaceRetrofit.java.
function createApp(collection) {
    const app = express();
    app.use(express.json());
    app.use(express.urlencoded({ extended: false }));

    app.get('/', (req, res) => {
        res.send('Manage Data Types API');
    });

    app.post('/insert', async (req, res) => {
        const user = {
            date: req.body.date,
            name: req.body.name,
            email: req.body.email,
            password: req.body.password
        };

        if (await collection.findOne({ email: user.email })) {
            return res.status(400).end();
        }

        await collection.insertOne(user);
        return res.status(200).end();
    });

    app.post('/selectuser', async (req, res) => {
        const user = await collection.findOne({
            email: req.body.email,
            password: req.body.password
        });

        if (!user) {
            return res.status(404).end();
        }

        return res.status(200).json({
            date: user.date,
            name: user.name,
            email: user.email,
            password: user.password
        });
    });

    app.get('/selectall', async (req, res) => {
        const users = await collection.find().toArray();
        return res.status(200).json(users);
    });

    app.post('/deleteuser', async (req, res) => {
        await collection.deleteOne({ email: req.body.email });
        return res.status(200).end();
    });

    app.get('/deleteall', async (req, res) => {
        await collection.deleteMany({});
        return res.status(200).end();
    });

    app.post('/update', async (req, res) => {
        await collection.updateOne(
            { email: req.body.email },
            { $set: { name: req.body.name, password: req.body.password } }
        );
        return res.status(200).end();
    });

    app.get('/checkexists', (req, res) => {
        res.status(200).json([]);
    });

    app.use((error, req, res, next) => {
        console.error(error);
        if (res.headersSent) {
            return next(error);
        }
        return res.status(500).json({ error: 'Internal server error' });
    });

    return app;
}

async function startServer() {
    // MONGODB_URI is preferred; `url` keeps existing deployments compatible.
    const connectionString = process.env.MONGODB_URI || process.env.url;
    if (!connectionString) {
        throw new Error('Set MONGODB_URI (or the legacy url variable) before starting the API.');
    }

    const client = new MongoClient(connectionString);
    await client.connect();

    // Preserve the database and collection names used by existing app data.
    const database = client.db('dbAndroidNode');
    const collection = database.collection('tableAndroidNode');
    const port = Number(process.env.PORT || 3003);
    const server = createApp(collection).listen(port, '0.0.0.0', () => {
        console.log(`Listening on port ${port}`);
    });

    const shutdown = async () => {
        server.close(async (error) => {
            if (error) {
                console.error('Error closing the HTTP server:', error);
                process.exitCode = 1;
            }
            await client.close();
        });
    };

    process.once('SIGINT', shutdown);
    process.once('SIGTERM', shutdown);
}

if (require.main === module) {
    startServer().catch((error) => {
        console.error('Unable to start the API:', error);
        process.exitCode = 1;
    });
}

module.exports = { createApp };
