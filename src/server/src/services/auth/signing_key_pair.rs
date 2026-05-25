use ed25519_dalek::{Signature, Signer, SigningKey, Verifier, VerifyingKey};
use rand_core::OsRng;

use crate::errors::error::{AppError, AppResult};

#[derive(Debug, Clone)]
pub struct SigningKeyPair {
    signing_key: SigningKey,
    verifying_key: VerifyingKey,
}

impl SigningKeyPair {
    pub fn generate() -> Self {
        let signing_key = SigningKey::generate(&mut OsRng);
        let verifying_key = signing_key.verifying_key();

        Self {
            signing_key,
            verifying_key,
        }
    }

    pub fn from_secret_hex(hex: &str) -> AppResult<Self> {
        let bytes = decode_hex_32(hex)?;
        let signing_key = SigningKey::from_bytes(&bytes);
        let verifying_key = signing_key.verifying_key();

        Ok(Self {
            signing_key,
            verifying_key,
        })
    }

    pub fn public_key_hex(&self) -> String {
        encode_hex(&self.verifying_key.to_bytes())
    }

    pub fn sign(&self, message: &[u8]) -> Signature {
        self.signing_key.sign(message)
    }

    pub fn verify(&self, message: &[u8], signature: &Signature) -> bool {
        self.verifying_key.verify(message, signature).is_ok()
    }
}

fn decode_hex_32(hex: &str) -> AppResult<[u8; 32]> {
    let hex = hex.trim();
    if hex.len() != 64 {
        return Err(AppError::SigningKeys);
    }

    let mut bytes = [0u8; 32];
    for (index, chunk) in hex.as_bytes().chunks(2).enumerate() {
        let hex_pair = std::str::from_utf8(chunk).map_err(|_| AppError::SigningKeys)?;
        bytes[index] = u8::from_str_radix(hex_pair, 16).map_err(|_| AppError::SigningKeys)?;
    }

    Ok(bytes)
}

fn encode_hex(bytes: &[u8]) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}
