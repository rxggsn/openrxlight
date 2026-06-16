ALTER TABLE "order" DROP COLUMN device_ids;
ALTER TABLE "order" ADD COLUMN additional_info JSONB NULL;
ALTER TABLE "order" ADD COLUMN payment_channel SMALLINT NULL;
ALTER TABLE "order" ADD COLUMN pre_fee INTEGER NULL;

COMMENT ON COLUMN "order".additional_info IS 'Additional info for the order (JSON format)';
COMMENT ON COLUMN "order".payment_channel IS 'Payment channel used for the order';
COMMENT ON COLUMN "order".pre_fee IS 'Pre-authorized fee amount for the order';