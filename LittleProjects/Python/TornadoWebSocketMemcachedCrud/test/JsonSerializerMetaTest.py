import unittest
import json


class JsonSerializerMetaTEst(unittest.TestCase):
    def test_to_and_from_json(self):
        from .TestClass import TestClass
        t = TestClass(1, 2)
        j = json.dumps(t, cls=t.json_encoder)
        o = json.loads(j, cls=t.json_decoder)

        self.assertEqual('{"a": 1, "b": 2, "__meta": "TestClass"}', j)
        self.assertIsInstance(o, TestClass)
        self.assertEqual(t, o)
        self.assertEqual(o, t)


if __name__ == '__main__':
    unittest.main()
