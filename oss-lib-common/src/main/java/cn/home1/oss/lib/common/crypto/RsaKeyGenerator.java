package cn.home1.oss.lib.common.crypto;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;

import cn.home1.oss.lib.common.CodecUtils;
import cn.home1.oss.lib.common.StringUtils;

/**
 * Created by zhanghaolun on 16/11/4.
 */
@Slf4j
public class RsaKeyGenerator implements KeyGenerator {

  private final Provider provider;
  @Getter
  private final String spec;
  @Getter
  private final String keyFormat;
  @Getter
  private final int keySize;
  @Getter
  private final String keyType;
  private RsaKey key;

  public RsaKeyGenerator(final String spec) {
    this.provider = Cryptos.provider();
    this.spec = spec;
    this.keyFormat = RsaKey.keyFormat(spec);
    this.keySize = RsaKey.keySize(spec);
    this.keyType = RsaKey.keyType(spec);
    Preconditions.checkArgument(RsaKey.SUPPORTED_PAIR_FORMATS.contains(this.keyFormat), "keyFormat " + this.keyFormat + " not supported.");
    Preconditions.checkArgument(RsaKey.KEY_TYPE_PAIR.equals(this.keyType), "keyType " + this.keyType + " not supported.");
  }

  @Override
  public KeyExpression generateKey() {
    final KeyExpression keyExpression = RsaKeyGenerator.generateRsaKeyPair( //
      this.provider, this.keyFormat, this.keySize);
    this.key = new RsaKey(keyExpression);
    return this.key.getKeyExpression();
  }

  @Override
  public KeyExpression getKey(final String spec) {
    if (this.key == null) {
      this.generateKey();
    }

    return this.key.getKey(spec);
  }

  public static KeyExpression convertPairFromPkcs8X509ToPkcs1(final KeyExpression pairPkcs8X509) {
    checkArgument(RsaKey.KEY_FORMAT_PKCS8_X509.equals(RsaKey.keyFormat(pairPkcs8X509.getSpec())), //
      "unsupported spec" + pairPkcs8X509.getSpec());
    final byte[] privateKeyPkcs8 = CodecUtils.decodeBase64(RsaKey.extractPrivateKey(pairPkcs8X509));
    final byte[] publicKeyX509 = CodecUtils.decodeBase64(RsaKey.extractPublicKey(pairPkcs8X509));
    final String privateKeyPem = convertPrivateKeyFromPkcs8ToPkcs1Pem(privateKeyPkcs8);
    final String publicKeyPem = convertPublicKeyFromX509ToPkcs1Pem(publicKeyX509);

    final int keySize = RsaKey.keySize(pairPkcs8X509.getSpec());
    final String spec = RsaKey.keySpec(RsaKey.KEY_FORMAT_PKCS1 + CryptoConstants.UNDERSCORE + RsaKey.KEY_FORMAT_PKCS1, keySize, RsaKey.KEY_TYPE_PAIR);
    final String value = StringUtils.dropComment(privateKeyPem, RsaKey.COMMENT_MARK) + CryptoConstants.COLON + StringUtils.dropComment(publicKeyPem, RsaKey.COMMENT_MARK);
    return new KeyExpression(spec, value);
  }

  @SneakyThrows
  public static String convertPrivateKeyFromPkcs8ToPkcs1Pem(final byte[] privateKeyPkcs8) {
    // Convert private key from PKCS8 to PKCS1:
    final ASN1Encodable encodable = PrivateKeyInfo.getInstance(privateKeyPkcs8).parsePrivateKey();
    return pem(encodable.toASN1Primitive().getEncoded(), RsaKey.KEY_FORMAT_PKCS1, RsaKey.KEY_TYPE_PRIVATE);
  }

  @SneakyThrows
  public static String convertPublicKeyFromX509ToPkcs1Pem(final byte[] publicKeyX509) {
    // Convert public key from X.509 SubjectPublicKeyInfo to PKCS1:
    final ASN1Primitive publicKeyPrimitive = SubjectPublicKeyInfo.getInstance(publicKeyX509).parsePublicKey();
    return pem(publicKeyPrimitive.getEncoded(), RsaKey.KEY_FORMAT_PKCS1, RsaKey.KEY_TYPE_PUBLIC);
  }

  public static KeyExpression generateRsaKeyPair( //
    final Provider provider, final String keyFormat, final int keySize //
  ) {
    final String privateKeyFormat = keyFormat.split(CryptoConstants.UNDERSCORE)[0];
    final String publicKeyFormat = keyFormat.split(CryptoConstants.UNDERSCORE)[1];

    final KeyExpression pairPkcs8X509 = generateRsaKeyPairPkcs8X509(provider, keySize);
    final KeyExpression pairPkcs1 = convertPairFromPkcs8X509ToPkcs1(pairPkcs8X509);

    final String privateKey;
    if (RsaKey.KEY_FORMAT_PKCS8.equals(privateKeyFormat)) {
      privateKey = RsaKey.extractPrivateKey(pairPkcs8X509);
    } else if (RsaKey.KEY_FORMAT_PKCS1.equals(privateKeyFormat)) {
      privateKey = RsaKey.extractPrivateKey(pairPkcs1);
    } else {
      throw new IllegalArgumentException("unsupported privateKeyFormat " + privateKeyFormat);
    }

    final String publicKey;
    if (RsaKey.KEY_FORMAT_X509.equals(publicKeyFormat)) {
      publicKey = RsaKey.extractPublicKey(pairPkcs8X509);
    } else if (RsaKey.KEY_FORMAT_PKCS1.equals(publicKeyFormat)) {
      publicKey = RsaKey.extractPublicKey(pairPkcs1);
    } else {
      throw new IllegalArgumentException("unsupported publicKeyFormat " + publicKeyFormat);
    }

    final String spec = RsaKey.keySpec(privateKeyFormat + CryptoConstants.UNDERSCORE + publicKeyFormat, keySize, RsaKey.KEY_TYPE_PAIR);
    final String value = privateKey + CryptoConstants.COLON + publicKey;
    return new KeyExpression(spec, value);
  }

  @SneakyThrows
  public static KeyPair generateRsaKeyPair(final Provider provider, final int keySize) {
    final KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(CryptoConstants.ALGO_RSA, provider);
    // final byte[] seed = DateFormatUtils.format(currentTimeMillis(), "yyyyMMdd").getBytes();
    keyPairGen.initialize(keySize, new SecureRandom());
    //keygen.initialize(keySize);
    return keyPairGen.generateKeyPair();
  }

  @SneakyThrows
  public static KeyExpression generateRsaKeyPairPkcs8X509(
    final Provider provider, //
    final int keySize //
  ) {
    final KeyPair pairPkcs8X509 = generateRsaKeyPair(provider, keySize);
    final byte[] privateKeyPkcs8 = pairPkcs8X509.getPrivate().getEncoded();
    final byte[] publicKeyX509 = pairPkcs8X509.getPublic().getEncoded();

    final String privateKeyPem = pem(privateKeyPkcs8, RsaKey.KEY_FORMAT_PKCS8, RsaKey.KEY_TYPE_PRIVATE);
    final String publicKeyPem = pem(publicKeyX509, RsaKey.KEY_FORMAT_X509, RsaKey.KEY_TYPE_PUBLIC);

    final String spec = RsaKey.keySpec(RsaKey.KEY_FORMAT_PKCS8 + CryptoConstants.UNDERSCORE + RsaKey.KEY_FORMAT_X509, keySize, RsaKey.KEY_TYPE_PAIR);
    final String value = StringUtils.dropComment(privateKeyPem, RsaKey.COMMENT_MARK) + CryptoConstants.COLON + StringUtils.dropComment(publicKeyPem, RsaKey.COMMENT_MARK);
    return new KeyExpression(spec, value);
  }

  @SneakyThrows
  public static String pem(final byte[] bytes, final String keyFormat, final String keyType) {
    final String type;
    if (RsaKey.KEY_TYPE_PRIVATE.equals(keyType)) {
      type = "RSA PRIVATE KEY";
    } else if (RsaKey.KEY_TYPE_PUBLIC.equals(keyType)) {
      if (RsaKey.KEY_FORMAT_X509.equals(keyFormat)) {
        type = "PUBLIC KEY";
      } else {
        type = "RSA PUBLIC KEY";
      }
    } else {
      throw new IllegalArgumentException("unsupported keyType " + keyType);
    }

    final StringWriter stringWriter = new StringWriter();
    try (final PemWriter pemWriter = new PemWriter(stringWriter)) {
      pemWriter.writeObject(new PemObject(type, bytes));
    }
    return stringWriter.toString();
  }
}
