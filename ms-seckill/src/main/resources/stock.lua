if (redis.call('hexists', KEYS[1], KEYS[2]) == 1) then
	local stock = tonumber(redis.call('hget', KEYS[1], KEYS[2]));
	if (stock > 0) then
	   redis.call('hincrby', KEYS[1], KEYS[2], -1);
	   return stock;
	end;
    return 0;
end;