#![warn(missing_docs)]
extern crate num_bigint;
extern crate num_traits;
extern crate openssl;
extern crate threshold_secret_sharing as tss;

use num_bigint::*;
use num_traits::*;
use openssl::rand::rand_bytes;

#[derive(Debug, Copy, Clone)]
pub enum Share {
    Arithmetic(u128),
    Shamirs(i64),
}
#[derive(Debug, Copy, Clone)]
pub enum ValueType {
    ArithmeticValue(u128),
    ShamirValue(i64),
}
pub enum SharingType {
    ArithmeticSharing(ArithmeticSharing),
    ShamirsSharing(ShamirsSharing),
    None,
}

#[derive(Debug)]
pub struct ShamirsSharing {
    generator: tss::shamir::ShamirSecretSharing,
    parties: usize,
    prime: i64,
}
impl ShamirsSharing {
    pub fn new(parties: usize) -> Self {
        let prime = 2i64.pow(19) - 1;
        //let prime = 2i64.pow(61) - 1;
        ShamirsSharing {
            generator: tss::shamir::ShamirSecretSharing {
                threshold: 1,
                share_count: parties,
                prime: prime,
            },
            parties,
            prime,
        }
    }

    pub fn share(&self, secret: ValueType) -> Vec<Share> {
        if let ValueType::ShamirValue(i) = secret {
            let s = self.generator.share(i);
            s.iter().map(|s| Share::Shamirs(*s)).collect()
        } else {
            panic!("Wrong value type. Should be Shamir's Value (i64)")
        }
    }

    pub fn reconstruct(&self, shares: &[Share]) -> ValueType {
        let s: Vec<i64> = shares
            .iter()
            .filter_map(|s| match s {
                Share::Arithmetic(_) => None,
                Share::Shamirs(i) => Some(*i),
            })
            .collect();
        let indices: Vec<usize> = (0..shares.len()).collect();
        ValueType::ShamirValue(self.generator.reconstruct(&indices, &s))
    }
}

#[derive(Debug)]
pub struct ArithmeticSharing {
    parties: usize,
    prime: u128,
}

impl ArithmeticSharing {
    pub fn new(parties: usize) -> Self {
        ArithmeticSharing {
            prime: 2u128.pow(127) - 1,
            parties,
        }
    }
    fn generate_random() -> (u128, [u8; 16]) {
        let mut buf = [0; 16];
        rand_bytes(&mut buf).expect("Can not generate randomness");
        let mut result: u128 = 0;
        for i in 0..16 {
            let v = buf[i] as u128;
            result |= v << i * 8;
        }
        (result, buf)
    }

    fn get_blind(&self) -> BigUint {
        let mut result = ArithmeticSharing::generate_random();
        while result.0 >= self.prime {
            result = ArithmeticSharing::generate_random();
        }
        BigUint::from_bytes_le(&result.1)
    }
    pub fn share(&self, secret: ValueType) -> Vec<Share> {
        if let ValueType::ArithmeticValue(i) = secret {
            assert!(i < self.prime);
            let mut shares: Vec<Share> = Vec::new();
            let mut secret = BigUint::from_u128(i).unwrap();
            for _ in 0..self.parties {
                let blind = self.get_blind();
                secret = secret
                    .checked_sub(&blind)
                    .unwrap_or(self.prime - &blind + &secret);
                shares.push(Share::Arithmetic(blind.to_u128().unwrap()));
            }
            shares.push(Share::Arithmetic(secret.to_u128().unwrap()));
            shares
        } else {
            panic!("Wrong value type. Should be Arithmetic Value (u128)")
        }
    }

    pub fn reconstruct(&self, shares: &[Share]) -> ValueType {
        let mut reconstructed: BigUint = num_traits::Zero::zero();
        for i in shares {
            match i {
                Share::Arithmetic(i) => {
                    let share = BigUint::from_u128(*i).unwrap();
                    reconstructed += share;
                }
                _ => {
                    continue;
                }
            }
        }
        reconstructed = reconstructed % BigUint::from_u128(self.prime).unwrap();
        ValueType::ArithmeticValue(reconstructed.to_u128().unwrap())
    }
}
