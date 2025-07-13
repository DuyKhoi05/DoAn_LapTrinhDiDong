const express = require('express');
const Stripe = require('stripe');
const cors = require('cors');

const app = express();
const stripe = Stripe('sk_test_51RgsTWJdGZiQ4fdVTLjDaMiA50GVQPvJchnIEDY7k0Q0fREJ3CbnMDkd1Efat8WFTegXrZ9Afw1NvOAUon1ACLJO00G9jgAfRg'); // 🔑 Thay bằng secret key của bạn

app.use(cors());
app.use(express.json());

app.post('/create-payment-intent', async (req, res) => {
  const { amount } = req.body;

  try {
    const paymentIntent = await stripe.paymentIntents.create({
      amount: amount,
      currency: 'usd',
    });

    res.send({ clientSecret: paymentIntent.client_secret });
  } catch (err) {
    res.status(500).send({ error: err.message });
  }
});

app.listen(4242, () => console.log('✅ Stripe server running at http://192.168.1.5:4242/create-payment-intent'));
