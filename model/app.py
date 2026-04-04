"""
Flask后端服务脚本
提供基础测试接口、计算器功能，以及模型推理接口代理。
"""

import base64
from pathlib import Path

import cv2
import numpy as np
from flask import Flask, request, jsonify

from predict_test import predict_image

app = Flask(__name__)


def _get_image_from_request():
    """从请求中获取图片：优先 file，其次 base64，最后 path。"""
    if request.files.get("image"):
        file_storage = request.files["image"]
        file_bytes = np.frombuffer(file_storage.read(), np.uint8)
        img = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)
        return img, file_storage.filename or "upload"

    data = request.get_json(silent=True) or {}
    image_b64 = data.get("image_base64")
    if image_b64:
        if "," in image_b64:
            image_b64 = image_b64.split(",", 1)[1]
        img_bytes = base64.b64decode(image_b64)
        img_array = np.frombuffer(img_bytes, np.uint8)
        img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)
        return img, data.get("image_name", "base64_image")

    image_path = data.get("image_path")
    if image_path:
        img = cv2.imread(image_path)
        return img, Path(image_path).name

    return None, None

@app.route('/test', methods=['GET'])
def hello_world():
    return 'Hello, World!'


@app.route('/predict', methods=['POST'])
def predict():
    img, image_name = _get_image_from_request()
    if img is None:
        return jsonify({
            'success': False,
            'message': '请通过 form-data 的 image、JSON 的 image_base64 或 image_path 传入图片',
            'predictions': [],
        }), 400

    json_result, _, _ = predict_image(img, image_name=image_name, show=False, save=False)
    return jsonify(json_result)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)