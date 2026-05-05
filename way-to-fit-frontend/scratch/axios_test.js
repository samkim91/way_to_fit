const axios = require('axios');

async function test() {
    const client = axios.create({
        baseURL: 'http://localhost:8080/api'
    });
    
    // 이 경우 baseURL의 path인 /api가 날아가는지 확인
    console.log('Request 1 (with leading slash):', client.getUri({ url: '/v1/test' }));
    console.log('Request 2 (without leading slash):', client.getUri({ url: 'v1/test' }));
}

test();
